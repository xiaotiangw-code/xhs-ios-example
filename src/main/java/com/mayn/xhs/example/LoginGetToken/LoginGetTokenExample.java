package com.mayn.xhs.example.LoginGetToken;

import com.mayn.xhs.example.common.PlatformAuth;

/**
 * 例 1：平台登录，获取 Bearer 令牌。
 * <p>sign-app 账户由 SQL 开设，无注册接口；登录参数见 {@link PlatformAuth}。</p>
 */
public class LoginGetTokenExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();
        assert token != null && token.length() > 16 : "令牌形态异常";
        System.out.println("[ok] 平台登录成功 token=" + token.substring(0, 12) + "…");
    }
}