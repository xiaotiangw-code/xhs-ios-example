package com.mayn.xhs.example.Logout;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.Http;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 7：登出账号（不删除设备）。
 * <p>尽力调下游 logout 注销服务端会话，清空账号凭据；phase→LOGGED_OUT。
 * 设备保留，可再快捷登录 / 验证码 / 密码登录。</p>
 */
public class LogoutExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        JSONObject body = new JSONObject().put("uniqueId", DemoConfig.DEVICE_ID);
        JSONObject data = Http.post(Urls.DEVICE_LOGOUT, body.toString(), token);

        String phase = data.optString("phase");
        assert "LOGGED_OUT".equals(phase) : "登出未到 LOGGED_OUT，实际 phase=" + phase;
        System.out.println("[ok] 登出成功 phase=" + phase);
    }
}