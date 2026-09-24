package com.xhs.example.sendComment;

import com.xhs.example.common.AbstractExample;
import com.xhs.example.common.Out;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例12：直播间发送评论 / 口令。
 *
 * <p><b>你要填的参数</b>——只改下面 4 个常量（从直播间采集数据里拿）：
 * 直播间 id、评论内容/口令、直播来源、前置来源。
 * 公共参数（平台账号 / 出站代理 / 设备）在 {@code config.properties}。</p>
 * <p><b>前置</b>：设备需已登录（先跑例2）。</p>
 *
 * <p>接口：POST live-room 域 /api/sns/v1/live/interaction/send_comment（form）。
 * 字段对齐 9.47.2 抓包（zb.har）。view_session_id 客户端自生成（{uid}_时间戳，{uid}
 * 由 sign-app 注入登录会话）；nickname 用 {nickname} 占位由服务端注入登录会话昵称。</p>
 */
public class SendCommentExample extends AbstractExample {

    // ① 你要填的参数（只改这里）
    static final String ROOM_ID = "your-room-id";         // 直播间 id
    static final String COMMENT_CONTENT = "your-comment"; // 评论内容 / 口令（可中文）
    static final String SOURCE_LIVE = "live_room";        // 直播来源
    static final String PRE_SOURCE = "shot_timeline";     // 直播前置来源

    public static void main(String[] args) throws Exception {
        new SendCommentExample().run();
    }

    // ══════════════════════════════════════════════
    //  ② 以下为业务差异点实现，一般不用改
    // ══════════════════════════════════════════════

    @Override
    protected String requestUrl() {
        return Urls.XHS_LIVE_ROOM + Urls.PATH_SEND_COMMENT;
    }

    @Override
    protected String requestParams() {
        // view_session_id 客户端自生成；{uid}/{nickname} 由 sign-app 注入登录会话值
        return "room_id=" + enc(ROOM_ID)
                + "&comment=" + enc(COMMENT_CONTENT)
                + "&comment_type=0&comment_extra_param="
                + "&source=" + enc(SOURCE_LIVE)
                + "&pre_source=" + enc(PRE_SOURCE)
                + "&view_session_id=" + "{uid}_" + System.currentTimeMillis()
                + "&role=0&nickname={nickname}&avatar="
                + "&at_users=" + enc("[]") + "&count=1";
    }

    @Override
    protected String contentType() {
        return "application/x-www-form-urlencoded; charset=utf-8";
    }

    @Override
    protected void onSuccess(JSONObject resp, String respText) {
        Out.kv("发送评论", "room_id", ROOM_ID, "comment", COMMENT_CONTENT,
                "code", String.valueOf(resp.optInt("code")));
        Out.raw("发送评论响应 data", resp.opt("data") == null ? resp.toString()
                : String.valueOf(resp.opt("data")));
    }
}