package com.xhs.example.logout;

import com.xhs.example.common.Http;
import com.xhs.example.common.Out;
import com.xhs.example.common.PlatformAuth;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 7：登出账号（不删除设备）。
 *
 * <p><b>你要填的参数</b>——只改 {@link #DEVICE_ID}：要登出的设备 uniqueId。
 * 公共参数（平台账号 / 出站代理）在 {@code config.properties}。</p>
 *
 * <p>尽力调下游 logout 注销服务端会话，清空账号凭据；phase→LOGGED_OUT。
 * 设备保留，可再快捷登录 / 验证码 / 密码登录。</p>
 */
public class LogoutExample {

    // ① 你要填的参数（只改这里）
    static final String DEVICE_ID = "your-device-unique-id";   // 要登出的设备 uniqueId

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        JSONObject body = new JSONObject().put("uniqueId", DEVICE_ID);
        JSONObject data = Http.post(Urls.DEVICE_LOGOUT, body.toString(), token);

        String phase = data.optString("phase");
        if (!"LOGGED_OUT".equals(phase)) {
            throw new IllegalStateException("登出未到 LOGGED_OUT，实际 phase=" + phase + "，响应: " + data);
        }
        Out.kv("登出", "phase", phase, "设备", DEVICE_ID);
        Out.json("登出响应 data", data);
        System.out.println("[ok] 登出成功 phase=" + phase);
    }
}