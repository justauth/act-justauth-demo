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
