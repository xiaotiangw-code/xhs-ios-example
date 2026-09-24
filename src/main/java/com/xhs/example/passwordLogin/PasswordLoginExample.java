package com.xhs.example.passwordLogin;

import com.xhs.example.common.Http;
import com.xhs.example.common.Out;
import com.xhs.example.common.PlatformAuth;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 2：小红书账号密码登录。
 *
 * <p><b>你要填的参数</b>——只改下面 4 个常量：目标设备 uniqueId（先跑例5 注册一台）、
 * 小红书手机号、账号密码、区号（国际号才需要改，默认 86）。
 * 公共参数（平台账号 / 出站代理）在 {@code config.properties}。</p>
 *
 * <p>要求设备已注册或曾登录；成功后 phase→LOGGED_IN，凭据（device_password 等）
 * 由服务端留存供快捷登录。</p>
 */
public class PasswordLoginExample {

    // ① 你要填的参数（只改这里）
    static final String DEVICE_ID = "your-device-unique-id";   // 已注册设备 uniqueId
    static final String PHONE     = "your-phone";              // 小红书手机号
    static final String PASSWORD  = "your-password";           // 小红书密码
    static final String ZONE      = "86";                      // 区号（国际号如 1/44/65，默认 86）

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        JSONObject body = new JSONObject()
                .put("uniqueId", DEVICE_ID)
                .put("phone", PHONE)
                .put("password", PASSWORD)
                .put("zone", ZONE);
        JSONObject data = Http.post(Urls.DEVICE_LOGIN_PASSWORD, body.toString(), token);

        String phase = data.optString("phase");
        if (!"LOGGED_IN".equals(phase)) {
            throw new IllegalStateException("密码登录未到 LOGGED_IN，实际 phase=" + phase + "，响应: " + data);
        }
        Out.kv("密码登录", "phase", phase, "手机号", PHONE, "区号", ZONE);
        Out.json("密码登录响应 data", data);
        System.out.println("[ok] 密码登录成功 phase=" + phase);
    }
}