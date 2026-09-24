package com.xhs.example.loginGetToken;

import com.xhs.example.common.PlatformAuth;
import com.xhs.example.common.Out;

/**
 * 例 1：平台登录，获取 Bearer 令牌。
 * <p>sign-app 账户由 SQL 开设，无注册接口；登录参数见 {@link PlatformAuth}。</p>
 */
public class LoginGetTokenExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();
        if (token == null || token.length() <= 16) {
            throw new IllegalStateException("令牌形态异常: " + token);
        }
        // 出参：登录响应的 data（token/expiresAt/nickname）
        Out.kv("平台登录", "token", token.substring(0, 16) + "…（共 " + token.length() + " 字符）",
                "nickname", PlatformAuth.lastNickname());
        System.out.println("[ok] 平台登录成功 token=" + token.substring(0, 12) + "…");
    }
}