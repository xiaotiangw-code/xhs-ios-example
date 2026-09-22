package com.mayn.xhs.example.QuickLogin;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.Http;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 4：快捷登录（免密免码）。
 * <p>凭据为上次登录成功后服务端滚动留存的 device_password + user_id，仅需 uniqueId 即可。
 * 设备从未登录过时无下发凭据，需外部直传 devicePassword。</p>
 */
public class QuickLoginExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        JSONObject body = new JSONObject().put("uniqueId", DemoConfig.DEVICE_ID);
        JSONObject data = Http.post(Urls.DEVICE_LOGIN_QUICK, body.toString(), token);

        String phase = data.optString("phase");
        assert "LOGGED_IN".equals(phase) : "快捷登录未到 LOGGED_IN，实际 phase=" + phase;
        System.out.println("[ok] 快捷登录成功 phase=" + phase);
    }
}