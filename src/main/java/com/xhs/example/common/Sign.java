package com.xhs.example.common;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 签名请求：调 sign-app /api/device/send 组装完整签名请求，服务端代发目标域。
 *
 * <p>正常形态（服务端 proxy-free=false）：sign 请求<b>不带 serverSend</b>，服务端返回
 * url + 签名头 + body + 设备代理（serverSent=false），客户端按该代理自行直发小红书。
 * 若服务端已代发（proxy-free=true 形态，回填 serverResponseBody），则直接取回填响应。</p>
 * <p>常见业务参数：host（目标域，缺省 edith）、body（POST 原文）、contentType。</p>
 */
public final class Sign {

    /** 最近一次组装出的签名请求（url + 头 + body），供示例打印出参。 */
    private static volatile String lastUrl = "";
    private static volatile Map<String, String> lastHeaders = new LinkedHashMap<String, String>();
    private static volatile String lastBody = "";
    /** 最近一次响应来源：true=服务端代发回填，false=客户端自发。 */
    private static volatile boolean lastServerSent = false;
    private static volatile int lastServerHttpCode = -1;

    private Sign() {
    }

    /** 打印最近一次签名请求全貌（url / 签名头 / body）+ 响应来源——示例用。 */
    public static void printLastSignedRequest() {
        System.out.println("[出参] 签名请求（sign 组装结果）: "
                + (lastServerSent ? "响应来源=服务端代发回填" : "响应来源=服务端代发"));
        System.out.println("  url: " + lastUrl);
        System.out.println("  签名头:");
        for (Map.Entry<String, String> e : lastHeaders.entrySet()) {
            String v = e.getValue() == null ? "" : e.getValue();
            System.out.println("    " + e.getKey() + ": "
                    + (v.length() > 80 ? v.substring(0, 80) + "…(" + v.length() + "字符)" : v));
        }
        System.out.println("  body: " + (lastBody == null || lastBody.isEmpty() ? "(空)"
                : (lastBody.length() > 200 ? lastBody.substring(0, 200) + "…" : lastBody)));
        if (lastServerSent) {
            System.out.println("  代发结果: 小红书 HTTP " + lastServerHttpCode);
        }
    }

    /**
     * 组装并取回响应。
     *
     * @param token    平台令牌
     * @param uniqueId 设备
     * @param host     目标域（Urls.XHS_REC / XHS_EDITH …）
     * @param path     接口路径（含 query），如 PATH_HOMEFEED
     * @param method   GET / POST
     * @param body     POST 请求体原文，GET 传空串
     * @return 小红书响应体
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body) {
        return signAndSend(token, uniqueId, host, path, method, body, null, null);
    }

    /**
     * 组装并取回响应（可显式指定请求 content-type，表单类接口用）。
     *
     * @param contentType 请求 Content-Type（如 application/x-www-form-urlencoded; charset=utf-8），可空
     * @return 小红书响应体
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body,
                                     String contentType) {
        return signAndSend(token, uniqueId, host, path, method, body, contentType, null);
    }

    /**
     * 组装并取回响应（可指定 content-type 与额外 bizParams，如 scenePoint）。
     *
     * @param contentType 请求 Content-Type（如 application/x-www-form-urlencoded; charset=utf-8），可空
     * @param extraBiz    合并进 bizParams 的额外字段（如 scenePoint=2942 覆盖场景点），可空
     * @return 小红书响应体
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body,
                                     String contentType, Map<String, String> extraBiz) {
        JSONObject req = new JSONObject()
                .put("uniqueId", uniqueId)
                .put("method", method)
                .put("path", path);   // ★ 不带 serverSend：客户端按 sign 返回的代理自行直发
        JSONObject biz = new JSONObject().put("host", host);
        if (body != null && !body.isEmpty()) {
            biz.put("body", body);
        }
        if (contentType != null && !contentType.isEmpty()) {
            biz.put("contentType", contentType);
        }
        if (extraBiz != null) {
            for (Map.Entry<String, String> e : extraBiz.entrySet()) {
                if (e.getValue() != null && !e.getValue().isEmpty()) {
                    biz.put(e.getKey(), e.getValue());
                }
            }
        }
        req.put("bizParams", biz);

        JSONObject sent = Http.post(Urls.DEVICE_SEND, req.toString(), token);
        int httpCode = sent.optInt("httpCode", -1);
        String responseBody = sent.optString("body", "");
        lastServerSent = true;
        lastServerHttpCode = httpCode;
        lastUrl = host + path;
        lastHeaders = new LinkedHashMap<String, String>();
        lastBody = body == null ? "" : body;
        if (httpCode < 200 || httpCode >= 300) {
            throw new AssertionError("小红书直发 HTTP " + httpCode + ": " + responseBody);
        }
        return responseBody;
    }
}
