# apache-httpclient-examples

## 1. 待整理

## 2. close()、shutdown()、releaseConnection()
```
    private void example() throws IOException {
        HttpClient httpClient = null;
        HttpGet method = null;
        HttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            method = new HttpGet("http://www.baidu.com");
            response = httpClient.execute(method);
            // ...
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            ((CloseableHttpResponse) response).close(); 
            
            response.getEntity().getContent().close();
            
            method.releaseConnection();
            
            httpClient.close();
        }
    }

```
### 2.1 `((CloseableHttpResponse) response).close()`与`response.getEntity().getContent().close()`
response.close()：关闭连接，且连接不可复用。
```
--- org.apache.http.impl.execchain.HttpResponseProxy.close

public void close() throws IOException {
    if (this.connHolder != null) {
        this.connHolder.close();
    }
}

--- org.apache.http.impl.execchain.ConnectionHolder.close

public void close() throws IOException {
    releaseConnection(false);   // 释放连接，false: 连接不可复用
}

private void releaseConnection(final boolean reusable) {
    if (this.released.compareAndSet(false, true)) { // 该连接未释放`released=false`，则执行释放连接的操作；否则，什么都不做
        synchronized (this.managedConn) {
            // reusable，true连接可复用；
            // manager，org.apache.http.impl.conn.PoolingHttpClientConnectionManager
            // managedConn，org.apache.http.impl.conn.CPoolProxy
            
            if (reusable) {
                this.manager.releaseConnection(this.managedConn,
                        this.state, this.validDuration, this.tunit); // 内部再正真判断连接是否可复用，若可以则放到conn-pool；否则关闭并移除该连接
            } else {
                try {
                    this.managedConn.close();  // 关闭socket，org.apache.http.impl.BHttpConnectionBase.close
                } catch (final IOException ex) {
                
                } finally {
                    this.manager.releaseConnection(
                            this.managedConn, null, 0, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}

--- org.apache.http.impl.conn.LoggingManagedHttpClientConnection
--- org.apache.http.impl.BHttpConnectionBase

public void close() throws IOException {
    final Socket socket = this.socketHolder.getAndSet(null);
    if (socket != null) {
        try {
            this.inbuffer.clear();
            this.outbuffer.flush();
            try {
                try {
                    socket.shutdownOutput();
                } catch (final IOException ignore) {
                }
                try {
                    socket.shutdownInput();
                } catch (final IOException ignore) {
                }
            } catch (final UnsupportedOperationException ignore) {
                // if one isn't supported, the other one isn't either
            }
        } finally {
            socket.close();
        }
    }
}
```
通过源代码可知，`response.close()`会关闭socket，所以连接并不能被复用。


content.close()：关闭InputStream，会触发`releaseConnection`，将该连接交还conn-poll管理。
```
--- org.apache.http.client.entity.LazyDecompressingInputStream

public void close() throws IOException {
    try {
        if (wrapperStream != null) {
            wrapperStream.close();
        }
    } finally {
        wrappedStream.close();  // org.apache.http.conn.EofSensorInputStream.close
    }
}

--- org.apache.http.conn.EofSensorInputStream

public void close() throws IOException {
    // tolerate multiple calls to close()
    selfClosed = true;
    checkClose();
}

--- org.apache.http.conn.EofSensorInputStream

protected void checkClose() throws IOException {

    final InputStream toCloseStream = wrappedStream;
    if (toCloseStream != null) {
        try {
            boolean scws = true; // should close wrapped stream?
            if (eofWatcher != null) {
                scws = eofWatcher.streamClosed(toCloseStream);  // org.apache.http.impl.execchain.ResponseEntityProxy.streamClosed
            }
            if (scws) {
                toCloseStream.close();
            }
        } finally {
            wrappedStream = null;
        }
    }
}

--- org.apache.http.impl.execchain.ResponseEntityProxy.streamClosed

public boolean streamClosed(final InputStream wrapped) throws IOException {
    try {
        final boolean open = connHolder != null && !connHolder.isReleased();
        // this assumes that closing the stream will
        // consume the remainder of the response body:
        try {
            if (wrapped != null) {
                wrapped.close();    // org.apache.http.impl.io.ChunkedInputStream.close
            }
            releaseConnection();
        } catch (final SocketException ex) {
            if (open) {
                throw ex;
            }
        }
    } catch (final IOException ex) {
        abortConnection();
        throw ex;
    } catch (final RuntimeException ex) {
        abortConnection();
        throw ex;
    } finally {
        cleanup();
    }
    return false;
}

public void releaseConnection() throws IOException {
    if (this.connHolder != null) {
        this.connHolder.releaseConnection();
    }
}

public void releaseConnection() {
    releaseConnection(this.reusable);   // 在执行请求响应后，通过响应的header来标记连接是否支持复用。
}

--- org.apache.http.impl.execchain.MainClientExec.execute  部分代码

   response = requestExecutor.execute(request, managedConn, context);

    // The connection is in or can be brought to a re-usable state.
    if (reuseStrategy.keepAlive(response, context)) {
        // Set the idle duration of this connection
        final long duration = keepAliveStrategy.getKeepAliveDuration(response, context);
        connHolder.setValidFor(duration, TimeUnit.MILLISECONDS);    // -1表示无失效时间
        connHolder.markReusable();  // 标记支持长连接，可复用
    } else {
        connHolder.markNonReusable();
    }
```
通过源代码可知，`content.close()`并未关闭连接，只是把连接交换给conn-pool，conn-pool判断当前连接是否可复用。
若可以复用，则加入`available`。否则，会关闭连接。（不管怎样，都会从`leased`中移除）