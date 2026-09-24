package com.xhs.example.smsCodeLogin;

import com.xhs.example.common.Http;
import com.xhs.example.common.Out;
import com.xhs.example.common.PlatformAuth;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

import java.util.Scanner;

/**
 * 例 3：短信验证码登录（发送验证码 → 输入收到的码 → 登录）。
 *
 * <p><b>你要填的参数</b>——只改下面 3 个常量：目标设备 uniqueId（先跑例5 注册一台）、
 * 小红书手机号、区号（国际号才需要改，默认 86）。
 * 公共参数（平台账号 / 出站代理）在 {@code config.properties}。</p>
 *
 * <p>分两段：/login/vfc-code 发码（type=login），/login/code 用码登录。</p>
 */
public class SmsCodeLoginExample {

    // ① 你要填的参数（只改这里）
    static final String DEVICE_ID = "your-device-unique-id";   // 已注册设备 uniqueId
    static final String PHONE     = "your-phone";              // 小红书手机号
    static final String ZONE      = "86";                      // 区号（国际号如 1/44/65，默认 86）

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        // ① 发送验证码
        JSONObject send = new JSONObject()
                .put("uniqueId", DEVICE_ID)
                .put("phone", PHONE)
                .put("zone", ZONE)
                .put("type", "login");
        JSONObject sendData = Http.post(Urls.DEVICE_LOGIN_VFC, send.toString(), token);
        Out.json("发送验证码响应 data", sendData);
        System.out.println("[ok] 验证码已发送至 " + PHONE);

        // ② 输入验证码登录（仅验证码这一步需要键盘输入）
        System.out.print("请输入收到的验证码: ");
        String code = new Scanner(System.in).nextLine().trim();

        JSONObject login = new JSONObject()
                .put("uniqueId", DEVICE_ID)
                .put("phone", PHONE)
                .put("zone", ZONE)
                .put("code", code)
                .put("type", "login");
        JSONObject data = Http.post(Urls.DEVICE_LOGIN_CODE, login.toString(), token);

        String phase = data.optString("phase");
        if (!"LOGGED_IN".equals(phase)) {
            throw new IllegalStateException("验证码登录未到 LOGGED_IN，实际 phase=" + phase + "，响应: " + data);
        }
        Out.kv("验证码登录", "phase", phase, "手机号", PHONE);
        Out.json("验证码登录响应 data", data);
        System.out.println("[ok] 验证码登录成功 phase=" + phase);
    }
}