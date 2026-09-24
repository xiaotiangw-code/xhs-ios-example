package com.xhs.example.noteDetailPreload;

import com.xhs.example.common.AbstractExample;
import com.xhs.example.common.Out;
import com.xhs.example.common.Sign;
import com.xhs.example.common.Urls;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 例10：笔记详情预加载（detailfeed/preload）。
 *
 * <p><b>你要填的参数</b>——本示例无需业务参数。它先取首页 feed 前若干条笔记的
 * id+xsec_token+type，再批量 POST edith 域 detailfeed/preload，响应 data.preload_map
 * （key=笔记id，value=完整详情）。对应抓包（rd.har）。</p>
 * <p>公共参数（平台账号 / 出站代理）在 config.properties 配一次。</p>
 */
public class NoteDetailPreloadExample extends AbstractExample {

    // ══════════════════════════════════════════════
    //  ① 可调参数（一般用默认即可）
    // ══════════════════════════════════════════════
    /** 一次预加载的笔记条数（从首页 feed 里取前若干条）。 */
    static final int PRELOAD_COUNT = 10;
    /** video 类型无 title 字段时，回退打印 desc 的最大字数。 */
    static final int DESC_FALLBACK = 30;

    public static void main(String[] args) throws Exception {
        new NoteDetailPreloadExample().run();
    }

    // ══════════════════════════════════════════════
    //  ② 业务差异点实现（两步合成一次发送，一般不用改）
    // ══════════════════════════════════════════════

    @Override
    protected String requestUrl() {
        return Urls.XHS_EDITH + Urls.PATH_PRELOAD;
    }

    @Override
    protected String contentType() {
        return "application/x-www-form-urlencoded; charset=utf-8";
    }

    /** 两段式：先取首页 feed 拿笔记 id+xsec_token，再批量 preload。 */
    @Override
    protected String send(String token, String uniqueId) throws Exception {
        // 1. 取首页 feed 拿笔记 id + xsec_token + type
        String homefeed = Sign.signAndSend(token, uniqueId, Urls.XHS_REC, Urls.PATH_HOMEFEED, "GET", "");
        JSONObject hf = new JSONObject(homefeed);
        JSONArray items = hf.optJSONArray("data");
        if (items == null && hf.optJSONObject("data") != null) {
            items = hf.optJSONObject("data").optJSONArray("items");
        }
        // 2. 组装 preload 的 data 数组（只取有 id 和 xsec_token 的条目）
        JSONArray dataArr = new JSONArray();
        for (int i = 0; i < items.length() && dataArr.length() < PRELOAD_COUNT; i++) {
            JSONObject it = items.optJSONObject(i);
            if (it == null) {
                continue;
            }
            String id = it.optString("id");
            String xsec = it.optString("xsec_token");
            if (id.isEmpty() || xsec.isEmpty()) {
                continue;
            }
            dataArr.put(new JSONObject()
                    .put("type", it.optString("type", "normal"))
                    .put("id", id)
                    .put("xsec_token", xsec));
        }
        if (dataArr.isEmpty()) {
            throw new IllegalStateException("首页未取到带 xsec_token 的笔记");
        }
        System.out.println("[i] 组装 " + dataArr.length() + " 条笔记详情预加载");

        // 3. POST detailfeed/preload（form；content-type 必须带，否则解析不到 data）
        String body = "data=" + URLEncoder.encode(dataArr.toString(), StandardCharsets.UTF_8.name())
                + "&source=main";
        return Sign.signAndSend(token, uniqueId, Urls.XHS_EDITH, Urls.PATH_PRELOAD,
                "POST", body, "application/x-www-form-urlencoded; charset=utf-8", Sign.SERVER_SEND);
    }

    @Override
    protected void onSuccess(JSONObject resp, String respText) {
        JSONObject data = resp.optJSONObject("data");
        JSONObject preloadMap = data == null ? null : data.optJSONObject("preload_map");
        if (data == null || preloadMap == null || preloadMap.isEmpty()) {
            // 空结果诊断：打出响应全貌，便于确认是登录态门槛还是别的
            Out.kv("笔记详情预加载", "code", String.valueOf(resp.optInt("code")),
                    "data字段数", String.valueOf(data == null ? 0 : data.length()),
                    "data字段名", data == null ? "" : String.valueOf(data.keySet()));
            Out.raw("preload 原始响应(data)", data == null ? "{}" : data.toString());
            System.out.println("[warn] preload_map 为空，见上面原始响应");
            return;
        }
        Out.kv("笔记详情预加载", "预加载笔记数", String.valueOf(preloadMap.length()));
        for (String noteId : preloadMap.keySet()) {
            JSONObject n = preloadMap.optJSONObject(noteId);
            if (n == null) {
                continue;
            }
            Out.kv("  preload[" + noteId + "]", "标题", titleOf(n),
                    "类型", n.optString("type"),
                    "作者id", n.optString("user_id"),
                    "分享", String.valueOf(n.optInt("share_count")),
                    "评论", String.valueOf(n.optInt("comments_count")),
                    "收藏", String.valueOf(n.optInt("collected_count")));
        }
    }

    /** 标题：normal 有 title 字段；video 该接口不返回 title，回退取 desc 前若干字。 */
    static String titleOf(JSONObject n) {
        String title = n.optString("title");
        if (!title.isEmpty()) {
            return title;
        }
        String desc = n.optString("desc").replace("\n", " ").trim();
        if (desc.isEmpty()) {
            return "(无标题/无描述)";
        }
        return desc.length() <= DESC_FALLBACK ? desc + "…(desc)"
                : desc.substring(0, DESC_FALLBACK) + "…(desc)";
    }
}