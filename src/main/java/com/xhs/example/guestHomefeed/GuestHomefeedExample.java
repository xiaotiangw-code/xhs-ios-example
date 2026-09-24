package com.xhs.example.guestHomefeed;

import com.xhs.example.common.AbstractExample;
import com.xhs.example.common.Out;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 8：游客请求首页（homefeed）。
 *
 * <p><b>你要填的参数</b>——本示例无需业务参数（游客态首页，注册设备后直发即可）。
 * 公共参数（平台账号 / 出站代理）在 config.properties 配一次。</p>
 *
 * <p>接口：GET rec 域 /api/sns/v6/homefeed（游客态首页推荐流）。</p>
 */
public class GuestHomefeedExample extends AbstractExample {

    public static void main(String[] args) throws Exception {
        new GuestHomefeedExample().run();
    }

    // ══════════════════════════════════════════════
    //  业务差异点实现（GET 首页，一般不用改）
    // ══════════════════════════════════════════════

    @Override
    protected String requestUrl() {
        return Urls.XHS_REC + Urls.PATH_HOMEFEED;
    }

    @Override
    protected String method() {
        return "GET";
    }

    @Override
    protected void onSuccess(JSONObject resp, String respText) {
        org.json.JSONArray items = resp.optJSONArray("data");
        Out.kv("游客首页", "HTTP响应字符数", String.valueOf(respText.length()),
                "笔记条数", String.valueOf(items == null ? 0 : items.length()));
        Out.list("首页笔记", items, "id", "type", "name", "likes", "user_id");
    }
}