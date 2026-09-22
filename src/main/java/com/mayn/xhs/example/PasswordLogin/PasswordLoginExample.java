package com.mayn.xhs.example.PasswordLogin;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.Http;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 2：小红书账号密码登录。
 * <p>要求设备已注册或曾登录；成功后 phase→LOGGED_IN，凭据（device_password 等）由服务端留存供快捷登录。</p>
 */
public class PasswordLoginExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        JSONObject body = new JSONObject()
                .put("uniqueId", DemoConfig.DEVICE_ID)
                .put("phone", DemoConfig.PHONE)
                .put("password", DemoConfig.PASSWORD);
        JSONObject data = Http.post(Urls.DEVICE_LOGIN_PASSWORD, body.toString(), token);

        String phase = data.optString("phase");
        assert "LOGGED_IN".equals(phase) : "密码登录未到 LOGGED_IN，实际 phase=" + phase;
        System.out.println("[ok] 密码登录成功 phase=" + phase);
    }
}