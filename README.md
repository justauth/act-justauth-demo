# act-justauth-demo

> 此 demo 主要为了演示 ActFramework 如何通过 JustAuth 快速集成第三方平台的登录。
>
> 如果有小伙伴是基于 Spring Boot 的可以参考这个 [**` demo`**](https://github.com/xkcoding/spring-boot-demo/tree/master/spring-boot-demo-social)
>
> https://github.com/xkcoding/spring-boot-demo/tree/master/spring-boot-demo-social
>
> 如果有小伙伴是基于 JFinal 的可以参考这个 [**` demo`**](https://github.com/xkcoding/jfinal-justauth-demo)
>
> https://github.com/xkcoding/jfinal-justauth-demo

## 步骤

### 0. 环境搭建

参考 [**`环境准备`**](https://github.com/xkcoding/spring-boot-demo/tree/master/spring-boot-demo-social#1-%E7%8E%AF%E5%A2%83%E5%87%86%E5%A4%87)

### 1. 创建工程

使用 idea 或者 eclipse 创建一个最简单的 maven 工程

### 2. 添加依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.xkcoding</groupId>
  <artifactId>act-justauth-demo</artifactId>
  <version>1.0-SNAPSHOT</version>

  <parent>
    <groupId>org.actframework</groupId>
    <artifactId>act-starter-parent</artifactId>
    <version>1.8.26.0</version>
  </parent>

  <properties>
    <!--maven配置信息-->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <!--JustAuth版本-->
    <justauth.version>1.9.5</justauth.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>me.zhyd.oauth</groupId>
      <artifactId>JustAuth</artifactId>
      <version>${justauth.version}</version>
    </dependency>
  </dependencies>

  <repositories>
    <!--阿里云私服-->
    <repository>
      <id>aliyun</id>
      <name>aliyun</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </repository>
    <!--xkcoding 私服-->
    <repository>
      <id>xkcoding-nexus</id>
      <name>xkcoding nexus</name>
      <url>https://nexus.xkcoding.com/repository/maven-public/</url>
      <releases>
        <enabled>true</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
</project>
```

### 3. 添加Action类

```java
package com.xkcoding.demo.action;

import act.Act;
import act.conf.AppConfig;
import act.controller.annotation.UrlContext;
import cn.hutool.core.lang.Dict;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthMiRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.ResponseContentType;

/**
 * <p>
 * 第三方登录请求处理
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-07-22 18:30
 */
@UrlContext("/oauth")
@SuppressWarnings("unused")
public class OAuthAction {

    /**
     * 登录类型
     */
    @GetAction
    @ResponseContentType(H.MediaType.JSON)
    public Dict loginType(H.Response response) {
        return Dict.create().set("QQ登录", "http://oauth.xkcoding.com/demo/oauth/login/qq").set("GitHub登录", "http://oauth.xkcoding.com/demo/oauth/login/github").set("小米登录", "http://oauth.xkcoding.com/demo/oauth/login/mi");
    }

    /**
     * 认证页面
     *
     * @param source   来源 {@link AuthSource}
     * @param response response
     */
    @GetAction("login/{source}")
    public void renderAuth(AuthSource source, H.Response response) {
        AuthRequest authRequest = getAuthRequest(source);
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    /**
     * 登录成功后的回调
     *
     * @param source   来源 {@link AuthSource}
     * @param callback 携带返回的信息 {@link AuthCallback}
     * @return 登录成功后的信息
     */
    @GetAction("{source}/callback")
    @ResponseContentType(H.MediaType.JSON)
    public AuthResponse login(AuthSource source, AuthCallback callback) {
        AuthRequest authRequest = getAuthRequest(source);
        AuthResponse response = authRequest.login(callback);
        return response;
    }

    /**
     * 获取AuthRequest
     *
     * @param source 来源 {@link AuthSource}
     * @return {@link AuthRequest}
     */
    private AuthRequest getAuthRequest(AuthSource source) {
        AuthConfig config = getAuthConfig(source);
        switch (source) {
            case QQ:
                return new AuthQqRequest(config);
            case GITHUB:
                return new AuthGithubRequest(config);
            case MI:
                return new AuthMiRequest(config);
            default:
                throw new RuntimeException("暂不支持的第三方登录");
        }
    }

    private AuthConfig getAuthConfig(AuthSource source) {
        AppConfig appConfig = Act.appConfig();
        String type = source.name().toLowerCase();
        String clientId = $.convert(appConfig.get("oauth." + type + ".client-id")).toString();
        String clientSecret = $.convert(appConfig.get("oauth." + type + ".client-secret")).toString();
        String redirectUri = $.convert(appConfig.get("oauth." + type + ".redirect-uri")).toString();
        return AuthConfig.builder().clientId(clientId).clientSecret(clientSecret).redirectUri(redirectUri).build();
    }
}
```

### 4. 添加配置文件

```properties
# all properties please see: https://github.com/actframework/archetype-support/blob/act-archetype-support-1.8.7.0/src/main/resources/archetype-resources/src/main/resources/conf/app.properties
http.port=8080
url.context=/demo
oauth.qq.client-id=10*******85
oauth.qq.client-secret=1f7d0*********************3cc2d629e
oauth.qq.redirect-uri=http://oauth.xkcoding.com/demo/oauth/qq/callback
oauth.github.client-id=2d2**************d5f01086
oauth.github.client-secret=5a291************************7871306d1
oauth.github.redirect-uri=http://oauth.xkcoding.com/demo/oauth/github/callback
oauth.mi.client-id=28823**************2994
oauth.mi.client-secret=nFeT*****************
oauth.mi.redirect-uri=http://oauth.xkcoding.com/demo/oauth/mi/callback
```

### 5. 程序入口类

```java
package com.xkcoding.demo;

import act.Act;
import org.osgl.http.H;
import org.osgl.mvc.annotation.GetAction;

/**
 * <p>
 * Act 程序入口
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-07-22 17:36
 */
@SuppressWarnings("unused")
public class AppEntry {

    @GetAction("/test")
    public void hello(H.Response response) {
        response.writeText("hello, act-framework!");
    }

    public static void main(String[] args) throws Exception {
        Act.start("act-justauth-demo");
    }
}
```

Enjoy ~

访问地址：http://localhost:8080/demo/oauth