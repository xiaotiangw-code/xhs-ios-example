package com.xhs.example.common;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 签名请求：调 sign-app /api/device/sign 组装完整签名请求。
 * <p>默认走「从服务器代发请求」（{@link #SERVER_SEND}=true）：sign-app 组装并签名后**直接发往小红书**，
 * 把真实响应回填到 {@code serverResponse*} 字段；客户端取响应体即可，不必自己发。
 * 置 false 则只组装签名（返回 url/headers/body），由客户端自己直发目标域（响应不回传服务端）。</p>
 * <p>常见业务参数：host（目标域，缺省 edith）、body（POST 原文）、contentType。</p>
 */
public final class Sign {

    /**
     * 从服务器代发请求开关（默认 <b>true</b>）：
     * <ul>
     *   <li>true —— 服务端代发：需该设备已配置代理（或服务端放开 proxy-free 直连），
     *       否则 sign-app 直接返回业务提示「设备未配置代理」；</li>
     *   <li>false —— 只组装签名，客户端自己直发（设备配了代理时响应会回填 proxy 供客户端出站用）。</li>
     * </ul>
     */
    public static final boolean SERVER_SEND = true;

    /** 最近一次组装出的签名请求（url + 头 + body），供示例打印出参。 */
    private static volatile String lastUrl = "";
    private static volatile Map<String, String> lastHeaders = new LinkedHashMap<String, String>();
    private static volatile String lastBody = "";
    /** 最近一次是否由服务端代发；代发的真实 HTTP 状态码。 */
    private static volatile boolean lastServerSent = false;
    private static volatile int lastServerHttpCode = -1;

    private Sign() {
    }

    /** 打印最近一次签名请求全貌（url / 签名头 / body）+ 代发结果——示例用，看"sign 到底组了什么"。 */
    public static void printLastSignedRequest() {
        System.out.println("[出参] 签名请求（sign 组装结果）: "
                + (lastServerSent ? "服务端代发（serverSend=true）" : "客户端自发（serverSend=false）"));
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
     * 组装并取回响应（默认 {@link #SERVER_SEND}=true：由服务端代发）。
     *
     * @param token    平台令牌
     * @param uniqueId 设备
     * @param host     目标域（Urls.XHS_REC / XHS_EDITH …）
     * @param path     接口路径（含 query），如 PATH_HOMEFEED
     * @param method   GET / POST
     * @param body     POST 请求体原文，GET 传空串
     * @return 小红书响应体（代发时=服务端回填的真实响应；自发时=客户端直发所得）
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body) {
        return signAndSend(token, uniqueId, host, path, method, body, null, SERVER_SEND);
    }

    /**
     * 组装并按 {@code serverSend} 取回响应。
     *
     * @param serverSend true=服务端代发（默认）；false=仅组装，客户端自己直发
     * @return 小红书响应体
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body, boolean serverSend) {
        return signAndSend(token, uniqueId, host, path, method, body, null, serverSend);
    }

    /**
     * 组装并按 {@code serverSend} 取回响应（可显式指定请求 content-type，表单类接口用）。
     *
     * @param contentType 请求 Content-Type（如 application/x-www-form-urlencoded; charset=utf-8），可空
     * @param serverSend  true=服务端代发（默认）；false=仅组装，客户端自己直发
     * @return 小红书响应体
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body,
                                     String contentType, boolean serverSend) {
        return signAndSend(token, uniqueId, host, path, method, body, contentType, serverSend, null);
    }

    /**
     * 组装并按 {@code serverSend} 取回响应（可指定 content-type 与额外 bizParams，如 scenePoint）。
     *
     * @param contentType 请求 Content-Type（如 application/x-www-form-urlencoded; charset=utf-8），可空
     * @param serverSend  true=服务端代发（默认）；false=仅组装，客户端自己直发
     * @param extraBiz    合并进 bizParams 的额外字段（如 scenePoint=2942 覆盖场景点），可空
     * @return 小红书响应体
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body,
                                     String contentType, boolean serverSend,
                                     Map<String, String> extraBiz) {
        JSONObject req = new JSONObject()
                .put("uniqueId", uniqueId)
                .put("method", method)
                .put("path", path)
                .put("serverSend", serverSend);
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

        JSONObject sign = Http.post(Urls.DEVICE_SIGN, req.toString(), token);
        String url = sign.optString("url");
        if (url.isEmpty()) {
            throw new IllegalStateException("签名未返回 url，响应: " + sign);
        }

        Map<String, String> headers = new LinkedHashMap<String, String>();
        JSONObject hdr = sign.optJSONObject("headers");
        if (hdr != null) {
            for (String k : hdr.keySet()) {
                headers.put(k, hdr.optString(k));
            }
        }
        String reqBody = sign.optString("body");
        lastUrl = url;
        lastHeaders = headers;
        lastBody = reqBody;
        lastServerSent = sign.optBoolean("serverSent", false);
        lastServerHttpCode = lastServerSent ? sign.optInt("serverHttpCode", -1) : -1;

        if (lastServerSent) {
            // 服务端已代发：真实响应在 serverResponseBody，客户端不再自发
            return sign.optString("serverResponseBody");
        }
        // 未代发：客户端直发目标域
        return Http.sendSigned(url, headers, reqBody);
    }
}