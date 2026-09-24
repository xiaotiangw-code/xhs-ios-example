package com.xhs.example.common;

import org.json.JSONObject;

/**
 * 平台鉴权：登录换取 Bearer 令牌。
 * <p>sign-app 账户由 SQL 开设，无注册接口；账号 / 密码 / 机器码从 config.properties 读取。</p>
 */
public final class PlatformAuth {

    /** 平台账号（sign-app 账户由 SQL 开设；见 config.properties 的 platform.*）。 */
    public static final String USERNAME = Config.get("platform.username", "");
    public static final String PASSWORD = Config.get("platform.password", "");
    public static final String MACHINE_CODE = Config.get("platform.machineCode", "");

    /** 最近一次登录响应里的昵称（供示例打印出参）。 */
    private static volatile String lastNickname = "";

    private PlatformAuth() {
    }

    /** @return 最近一次登录的昵称（未登录时为空串） */
    public static String lastNickname() {
        return lastNickname;
    }

    /** 平台登录，返回 Bearer 令牌。 */
    public static String login() {
        JSONObject body = new JSONObject()
                .put("username", USERNAME)
                .put("password", PASSWORD)
                .put("machineCode", MACHINE_CODE);
        JSONObject data = Http.postNoAuth(Urls.AUTH_LOGIN, body.toString());
        String token = data.optString("token");
        lastNickname = data.optString("nickname");
        if (token.isEmpty()) {
            throw new IllegalStateException("平台登录未返回 token，响应: " + data);
        }
        return token;
    }
}