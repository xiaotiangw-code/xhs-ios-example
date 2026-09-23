package com.mayn.xhs.example.SmsCodeLogin;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.Http;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Urls;
import com.mayn.xhs.example.common.Out;
import org.json.JSONObject;

import java.util.Scanner;

/**
 * 例 3：短信验证码登录（发送验证码 → 输入收到的码 → 登录）。
 * <p>分两段：/login/vfc-code 发码（type=login），/login/code 用码登录。手机号见 {@link DemoConfig#PHONE}。</p>
 */
public class SmsCodeLoginExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        // ① 发送验证码
        JSONObject send = new JSONObject()
                .put("uniqueId", DemoConfig.DEVICE_ID)
                .put("phone", DemoConfig.PHONE)
                .put("type", "login");
        JSONObject sendData = Http.post(Urls.DEVICE_LOGIN_VFC, send.toString(), token);
        Out.json("发送验证码响应 data", sendData);
        System.out.println("[ok] 验证码已发送至 " + DemoConfig.PHONE);

        // ② 输入验证码登录（仅验证码这一步需要键盘输入）
        System.out.print("请输入收到的验证码: ");
        String code = new Scanner(System.in).nextLine().trim();

        JSONObject login = new JSONObject()
                .put("uniqueId", DemoConfig.DEVICE_ID)
                .put("phone", DemoConfig.PHONE)
                .put("code", code)
                .put("type", "login");
        JSONObject data = Http.post(Urls.DEVICE_LOGIN_CODE, login.toString(), token);

        String phase = data.optString("phase");
        assert "LOGGED_IN".equals(phase) : "验证码登录未到 LOGGED_IN，实际 phase=" + phase;
        Out.kv("验证码登录", "phase", phase, "手机号", DemoConfig.PHONE);
        Out.json("验证码登录响应 data", data);
        System.out.println("[ok] 验证码登录成功 phase=" + phase);
    }
}