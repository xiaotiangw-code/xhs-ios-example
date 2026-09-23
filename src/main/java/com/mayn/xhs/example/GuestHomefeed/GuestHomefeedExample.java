package com.mayn.xhs.example.GuestHomefeed;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.DeviceOps;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Sign;
import com.mayn.xhs.example.common.Urls;
import com.mayn.xhs.example.common.Out;
import org.json.JSONObject;

/**
 * 例 8：游客请求首页（homefeed）。
 * <p>注册一台游客设备，签名并直发 rec 域首页，断言拿到内容列表。
 * 游客态无需登录，仅需已注册设备。想复用已注册设备时，把 {@link DemoConfig#DEVICE_ID}
 * 填上即可（省去每次现场注册）。</p>
 */
public class GuestHomefeedExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        // 无指定设备时现场注册一台（可先跑例5拿到 uniqueId 填回 DemoConfig 复用）
        String uniqueId = DeviceOps.createAndRegister(token, null, DemoConfig.PROXY);
        System.out.println("[i] 游客设备 uniqueId=" + uniqueId);

        String respText = Sign.signAndSend(token, uniqueId, Urls.XHS_REC, Urls.PATH_HOMEFEED, "GET", "");
        JSONObject resp = new JSONObject(respText);
        assert resp.has("data") : "首页未返回内容: " + respText;
        // 出参：首页 feed 列表（id/类型/标题/作者/互动数）
        org.json.JSONArray items = resp.optJSONArray("data");
        Out.kv("游客首页", "HTTP响应字符数", String.valueOf(respText.length()),
                "笔记条数", String.valueOf(items == null ? 0 : items.length()));
        Out.list("首页笔记", items, "id", "type", "name", "likes", "user_id");
        System.out.println("[ok] 游客首页请求成功 data 字段存在");
    }
}