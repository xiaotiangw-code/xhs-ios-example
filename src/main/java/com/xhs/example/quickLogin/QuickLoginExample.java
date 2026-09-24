package com.xhs.example.quickLogin;

import com.xhs.example.common.Http;
import com.xhs.example.common.Out;
import com.xhs.example.common.PlatformAuth;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 4：快捷登录（免密免码）。
 *
 * <p><b>你要填的参数</b>——只改 {@link #DEVICE_ID}：曾登录过的小红书设备 uniqueId
 * （凭据为上次登录成功后服务端滚动留存的 device_password + user_id）。
 * 公共参数（平台账号 / 出站代理）在 {@code config.properties}。</p>
 */
public class QuickLoginExample {

    // ① 你要填的参数（只改这里）
    static final String DEVICE_ID = "your-device-unique-id";   // 已登录过的设备 uniqueId

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        JSONObject body = new JSONObject().put("uniqueId", DEVICE_ID);
        JSONObject data = Http.post(Urls.DEVICE_LOGIN_QUICK, body.toString(), token);

        String phase = data.optString("phase");
        if (!"LOGGED_IN".equals(phase)) {
            throw new IllegalStateException("快捷登录未到 LOGGED_IN，实际 phase=" + phase + "，响应: " + data);
        }
        Out.kv("快捷登录", "phase", phase, "设备", DEVICE_ID);
        Out.json("快捷登录响应 data", data);
        System.out.println("[ok] 快捷登录成功 phase=" + phase);
    }
}