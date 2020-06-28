# 【002】HTTP中的ContentType.md

+ [runoob, http-content-type](https://www.runoob.com/http/http-content-type.html)

Content-Type 标头告诉客户端实际返回的内容的内容类型。

## Content-Type的格式
```
type/subtype(;parameter)? type

# 可以拆解为三个部分，分别是主类型（type）、子类型（subtype）和参数（parameter）。
```

**主类型（type）：**  
主类型可以是任意的字符串，比如text。如果是*号则代表所有类型。

**子类型（subtype）：**  
子类型可以是任意的字符串，比如html。如果是*号则代表所有类型。

**参数（parameter）：**  
参数是可选的，可以在Content-Type中加入一些特殊的参数，比如Accept请求头的参数，常见的有用于设置字符编码的charset参数。
ex. `Content-Type: text/html;charset:utf-8;`


## FAQ
### spring MVC 中能否手动自定义设置 content-type？
- [从content-type设置看Spring MVC处理header的一个坑](https://www.cnblogs.com/kaiblog/p/7565231.html)

```text
@Controller
@RequestMapping("/provider")
public class ProviderController {

    @RequestMapping("/content-type")
    public String customContentType(HttpServletRequest request, HttpServletResponse response,
                     @RequestParam(defaultValue = "image/png") String contentType,
                     @RequestParam(defaultValue = "409839163") Long contentLength){
    
        response.setContentType(contentType);
        response.setContentLengthLong(contentLength);
        response.setHeader("custom-content-type", contentType);
        response.setHeader("custom-content-length", contentLength + "");
        
        return String.format("expect >>>> content-type: %s, content-length: %d", contentType, contentLength);
    }
}

# http://localhost:port/provider/content-type?contentType=image/png&contentLength=409839163

# chrome
Content-Length: 62
Content-Type: text/html;charset=UTF-8
custom-content-length: 409839163
custom-content-type: image/png

# apache-http, `null`表示key在header中不存在
Content-Type: application/json
Content-Length: null  
custom-content-length: null
custom-content-type: null
```


