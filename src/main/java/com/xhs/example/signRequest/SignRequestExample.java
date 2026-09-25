package com.xhs.example.signRequest;

import com.xhs.example.common.AbstractExample;
import com.xhs.example.common.Out;
import com.xhs.example.common.Sign;
import com.xhs.example.common.Urls;
import org.json.JSONObject;

/**
 * 例 6：签名并取回一个请求的响应（游客首页）。
 *
 * <p><b>你要填的参数</b>——本示例无需业务参数。它演示「签名到底组了什么」：用
 * {@link Sign#printLastSignedRequest()} 把组装出的 url / 签名头 / body 全打出来。</p>
 * <p>公共参数（平台账号 / 出站代理）在 config.properties 配一次。</p>
 */
public class SignRequestExample extends AbstractExample {

    public static void main(String[] args) throws Exception {
        new SignRequestExample().run();
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
        // 打印组装出的签名请求全貌（url + 签名头 + body + 代发结果）
        Sign.printLastSignedRequest();
        Out.json("小红书 homefeed 响应（结构）", resp);
        Out.list("首页笔记列表", resp.optJSONArray("data"), "id", "type", "name", "likes");
    }
}