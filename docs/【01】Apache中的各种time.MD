# 【01】Apache中的各种time

```JAVA
public abstract class AbstractHttpClientTestng {
     
    protected CloseableHttpClient httpClient = null;

    @BeforeTest
    public void before(){

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(config.connRequestTimeout)  // httpclient使用连接池来管理连接，这个时间就是从连接池获取连接的超时时间
                .setConnectTimeout(config.connTimeout)  // 连接建立时间，即三次握手完成时间
                .setSocketTimeout(config.socketTimeout)  // 连接建立开始传递数据后，数据包之间的最大等待间隔
                .build();

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(config.maxConnPerRoute)  // 每一个路由的连接上限 
                .setMaxConnTotal(config.maxConnTotal)  // 连接池中连接上限
                .setConnectionTimeToLive(10086, TimeUnit.SECONDS)

                //  可以看到在HttpClientBuilder进行build的时候，如果指定了开启清理功能，会创建一个连接池清理线程并运行它。
                // .evictIdleConnections(maxIdleTime, unit)     // 指定是否清理空闲连接
                // .evictExpiredConnections()   // 指定是否要清理过期连接，默认不启动清理线程(false)

                .build();

    }
}
```

## 1. ConnectionTimeToLive
1. javadocs中给出的参数解释
```
/**
 * Sets maximum time to live for persistent connections(google翻译：设置持久连接的最长生存时间)
 * <p>
 * Please note this value can be overridden by the {@link #setConnectionManager(
 *   org.apache.http.conn.HttpClientConnectionManager)} method.
 * </p>
 *
 * @since 4.4
 */
public final HttpClientBuilder setConnectionTimeToLive(final long connTimeToLive, final TimeUnit connTimeToLiveTimeUnit) {
    this.connTimeToLive = connTimeToLive;
    this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
    return this;
}
```

2. 通过`HttpClientBuilder#build()`可知，`ConnectionTimeToLive`参数只适用于`PoolingHttpClientConnectionManager`；
```
 final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactoryCopy)
                        .build(),
                    null,
                    null,
                    dnsResolver,
                    connTimeToLive,
                    connTimeToLiveTimeUnit != null ? connTimeToLiveTimeUnit : TimeUnit.MILLISECONDS);
```

3. 通过跟踪源码，发现`org.apache.http.pool.PoolEntry`
```java
package org.apache.http.pool;

public abstract class PoolEntry<T, C> {
    
    private final String id;
    private final T route;
    private final C conn;
    private final long created;
    private final long validityDeadline;

    private long updated;

    private long expiry;

    private volatile Object state;
    
     /**
     * Creates new {@code PoolEntry} instance.
     *
     * @param id unique identifier of the pool entry. May be {@code null}.
     * @param route route to the opposite endpoint.
     * @param conn the connection.
     * @param timeToLive maximum time to live. May be zero if the connection
     *   does not have an expiry deadline.
     * @param timeUnit time unit.
     */
    public PoolEntry(final String id, final T route, final C conn,
            final long timeToLive, final TimeUnit timeUnit) {
        super();
        Args.notNull(route, "Route");
        Args.notNull(conn, "Connection");
        Args.notNull(timeUnit, "Time unit");
        this.id = id;
        this.route = route;
        this.conn = conn;
        this.created = System.currentTimeMillis();
        this.updated = this.created;
        if (timeToLive > 0) {
            final long deadline = this.created + timeUnit.toMillis(timeToLive);
            // If the above overflows then default to Long.MAX_VALUE
            this.validityDeadline = deadline > 0 ? deadline : Long.MAX_VALUE;
        } else {
            this.validityDeadline = Long.MAX_VALUE;
        }
        this.expiry = this.validityDeadline;
    }
    
    public synchronized void updateExpiry(final long time, final TimeUnit timeUnit) {
        Args.notNull(timeUnit, "Time unit");
        this.updated = System.currentTimeMillis();
        final long newExpiry;
        if (time > 0) {
            newExpiry = this.updated + timeUnit.toMillis(time);
        } else {
            newExpiry = Long.MAX_VALUE;
        }
        this.expiry = Math.min(newExpiry, this.validityDeadline);
    }

    public synchronized boolean isExpired(final long now) {
        return now >= this.expiry;
    }
}
```

```java
package org.apache.http.impl.conn;

public class PoolingHttpClientConnectionManager
    implements HttpClientConnectionManager, ConnPoolControl<HttpRoute>, Closeable {
    
     @Override
    public void releaseConnection(
            final HttpClientConnection managedConn,
            final Object state,
            final long keepalive, final TimeUnit timeUnit) {
        Args.notNull(managedConn, "Managed connection");
        synchronized (managedConn) {
            final CPoolEntry entry = CPoolProxy.detach(managedConn);
            if (entry == null) {
                return;
            }
            final ManagedHttpClientConnection conn = entry.getConnection();
            try {
                if (conn.isOpen()) {
                    final TimeUnit effectiveUnit = timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS;
                    entry.setState(state);
                    entry.updateExpiry(keepalive, effectiveUnit);  // vergilyn, 2019-12-24 >>>> 更新`连接实例`的失效时间
                    if (this.log.isDebugEnabled()) {
                        final String s;
                        if (keepalive > 0) {
                            s = "for " + (double) effectiveUnit.toMillis(keepalive) / 1000 + " seconds";
                        } else {
                            s = "indefinitely";
                        }
                        this.log.debug("Connection " + format(entry) + " can be kept alive " + s);
                    }
                    conn.setSocketTimeout(0);
                }
            } finally {
                this.pool.release(entry, conn.isOpen() && entry.isRouteComplete());
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Connection released: " + format(entry) + formatStats(entry.getRoute()));
                }
            }
        }
    }
}
```

```
// org.apache.http.impl.execchain.MainClientExec#execute(...)

/* vergilyn, 2019-12-24 >>>> 
 * 服务端响应Header中携带`Keep-Alive: timeout=86400` （因为测试代码 ProviderApplication 中设置了 `server.tomcat.connection-timeout = 1d = 86400s`）。
 * duration即release参数。
 */
 
HttpResponse response;
// 省略代码...
response = requestExecutor.execute(request, managedConn, context);

// The connection is in or can be brought to a re-usable state.
if (reuseStrategy.keepAlive(response, context)) {
    // Set the idle duration of this connection
    final long duration = keepAliveStrategy.getKeepAliveDuration(response, context);
    if (this.log.isDebugEnabled()) {
        final String s;
        if (duration > 0) {
            s = "for " + duration + " " + TimeUnit.MILLISECONDS;
        } else {
            s = "indefinitely";
        }
        this.log.debug("Connection can be kept alive " + s);
    }
    connHolder.setValidFor(duration, TimeUnit.MILLISECONDS);  // vergilyn, 2019-12-24 >>>> 最终即是`releaseConnection(...)`的keepAlive参数
    connHolder.markReusable();
} else {
    connHolder.markNonReusable();
}
```

## 2. RequestConfig#ConnectionRequestTimeout

## 3. RequestConfig#ConnectTimeout

## 4. RequestConfig#SocketTimeout


## 参考
- [Stackoverflow, Setconnecttimeout Vs Setconnectiontimetolive Vs Setsockettimeout](https://stackoverflow.com/questions/31566851/setconnecttimeout-vs-setconnectiontimetolive-vs-setsockettimeout)
- [HttpClient 4.x Timeout](https://dzone.com/articles/httpclient-4x-timeout)