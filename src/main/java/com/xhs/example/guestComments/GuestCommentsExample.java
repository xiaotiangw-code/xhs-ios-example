package com.xhs.example.guestComments;

import com.xhs.example.common.AbstractExample;
import com.xhs.example.common.Out;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 9：游客请求评论页（一级评论列表）。
 *
 * <p><b>你要填的参数</b>——只改下面 {@link #NOTE_ID} 一个常量：
 * 目标笔记 id（拿笔记分享链接里的那串 id）。
 * 公共参数（平台账号 / 出站代理 / 设备）在 {@code config.properties}。</p>
 *
 * <p>接口：GET edith 域 /api/sns/v5/note/comment/list。</p>
 */
public class GuestCommentsExample extends AbstractExample {

    // ① 你要填的参数（只改这里）
    static final String NOTE_ID = "your-note-id";   // 目标笔记 id

    public static void main(String[] args) throws Exception {
        new GuestCommentsExample().run();
    }

    // ══════════════════════════════════════════════
    //  ② 以下为业务差异点实现，一般不用改
    // ══════════════════════════════════════════════

    @Override
    protected String requestUrl() {
        return Urls.XHS_EDITH + Urls.PATH_COMMENT_LIST + "?note_id=" + NOTE_ID + "&start=0&num=15";
    }

    @Override
    protected String method() {
        return "GET";
    }

    @Override
    protected void onSuccess(JSONObject resp, String respText) {
        JSONObject data = resp.optJSONObject("data");
        Out.kv("评论页", "note_id", NOTE_ID,
                "评论总数", String.valueOf(data == null ? -1 : data.optInt("total", -1)));
        Out.list("评论列表", data == null ? null : data.optJSONArray("comments"),
                "id", "content", "likes", "user_id");
    }
}