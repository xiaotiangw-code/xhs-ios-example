package com.mayn.xhs.example.GuestComments;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.Sign;
import com.mayn.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 9：游客请求评论页（一级评论列表）。
 * <p>签名单条直发 edith 域 {@code /api/sns/v5/note/comment/list}。
 * note_id 见 {@link DemoConfig#NOTE_ID}。演示"签名 + 直发 + 断言业务成功"的完整写法。</p>
 */
public class GuestCommentsExample {

    public static void main(String[] args) {
        String token = com.mayn.xhs.example.common.PlatformAuth.login();

        String path = Urls.PATH_COMMENT_LIST + "?note_id=" + DemoConfig.NOTE_ID + "&start=0&num=15";
        String respText = Sign.signAndSend(token, DemoConfig.DEVICE_ID, Urls.XHS_EDITH, path, "GET", "");

        JSONObject resp = new JSONObject(respText);
        assert resp.optInt("code", -1) == 0
                || resp.optJSONObject("data") != null : "评论请求未成功: " + respText;
        JSONObject data = resp.optJSONObject("data");
        assert data != null && (data.optJSONArray("comments") != null
                || data.has("total")) : "评论响应缺少 comments/total";
        System.out.println("[ok] 评论页请求成功 note_id=" + DemoConfig.NOTE_ID);
    }
}