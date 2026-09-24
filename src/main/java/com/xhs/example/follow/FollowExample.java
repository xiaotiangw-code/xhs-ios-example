package com.xhs.example.follow;

import com.xhs.example.common.AbstractExample;
import com.xhs.example.common.Out;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 例11：关注（直播间内关注主播）。
 *
 * <p><b>你要填的参数</b>——只改下面两个常量（从直播间采集数据里拿）：
 * {@link #HOST_ID} 主播 id、{@link #ROOM_ID} 直播间 id。
 * 公共参数（平台账号 / 出站代理 / 设备）在 {@code config.properties}。</p>
 * <p><b>前置</b>：设备需已登录（先跑例2）。</p>
 *
 * <p>接口：POST edith 域 /api/sns/v1/user/follow（form）。直播间内关注固定场景 point=2942，
 * 须覆盖 sign-app 默认场景（否则被判成 601 发现页）。字段对齐 9.47.2 抓包（zb.har）。</p>
 */
public class FollowExample extends AbstractExample {

    // ① 你要填的参数（只改这里）
    static final String HOST_ID = "your-host-id";   // 主播 id
    static final String ROOM_ID = "your-room-id";   // 直播间 id

    public static void main(String[] args) throws Exception {
        new FollowExample().run();
    }

    // ══════════════════════════════════════════════
    //  ② 以下为业务差异点实现，一般不用改
    // ══════════════════════════════════════════════

    @Override
    protected String requestUrl() {
        return Urls.XHS_EDITH + Urls.PATH_FOLLOW;
    }

    @Override
    protected String requestParams() {
        return "live_source=money&room_id=" + enc(ROOM_ID)
                + "&scene=live_view_page&source=LIVE&sub_scene=money&type=MANUAL"
                + "&userids=" + enc(HOST_ID);
    }

    @Override
    protected String contentType() {
        return "application/x-www-form-urlencoded; charset=utf-8";
    }

    @Override
    protected String method() {
        return "POST";
    }

    @Override
    protected Map<String, String> extraBiz() {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("scenePoint", "2942");   // 直播间内关注固定场景点
        return m;
    }

    @Override
    protected void onSuccess(JSONObject resp, String respText) {
        Out.kv("关注主播", "room_id", ROOM_ID, "host_id", HOST_ID,
                "code", String.valueOf(resp.optInt("code")));
        Out.raw("关注响应 data", resp.opt("data") == null ? resp.toString()
                : String.valueOf(resp.opt("data")));
    }
}