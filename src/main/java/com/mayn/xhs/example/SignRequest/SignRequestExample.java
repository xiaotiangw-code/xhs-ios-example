package com.mayn.xhs.example.SignRequest;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Sign;
import com.mayn.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 6：签名并直发一个请求（游客首页）。
 * <p>调 /api/device/sign 组装完整签名请求，再直发小红书 target 域，返回响应原文。
 * 这是最能体现"设备托管"的示例——客户端不持有任何设备材料，只凭 uniqueId。</p>
 */
public class SignRequestExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        String respText = Sign.signAndSend(token, DemoConfig.DEVICE_ID, Urls.XHS_REC,
                Urls.PATH_HOMEFEED, "GET", "");
        JSONObject resp = new JSONObject(respText);
        assert resp.optInt("code", -1) == 0 || resp.has("data") : "请求未成功: " + respText;
        System.out.println("[ok] 签名直发成功: " + respText.substring(0, Math.min(120, respText.length())) + "…");
    }
}