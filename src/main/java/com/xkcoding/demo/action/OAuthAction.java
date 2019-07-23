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
import me.zhyd.oauth.utils.AuthState;
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
        return Dict.create()
                .set("QQ登录", "http://oauth.xkcoding.com/demo/oauth/login/qq")
                .set("GitHub登录", "http://oauth.xkcoding.com/demo/oauth/login/github")
                .set("小米登录", "http://oauth.xkcoding.com/demo/oauth/login/mi");
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
        response.sendRedirect(authRequest.authorize());
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
        // 移除校验通过的state
        AuthState.delete(source);
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
        return AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .state(AuthState.create(source))
                .build();
    }
}
