package com.mayn.xhs.example.common;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 签名请求：调 sign-app /api/device/sign 组装完整签名请求，再直发小红书目标域。
 * <p>sign-app 只负责"组装 + 签名"，真正发往小红书由客户端完成（响应不回传服务端）。
 * 常见业务参数：host（目标域，缺省 edith）、body（POST 原文）、contentType。</p>
 */
public final class Sign {

    /** 最近一次组装出的签名请求（url + 头 + body），供示例打印出参。 */
    private static volatile String lastUrl = "";
    private static volatile Map<String, String> lastHeaders = new LinkedHashMap<String, String>();
    private static volatile String lastBody = "";

    private Sign() {
    }

    /** 打印最近一次签名请求全貌（url / 签名头 / body）——示例用，看"sign 到底组了什么"。 */
    public static void printLastSignedRequest() {
        System.out.println("[出参] 签名请求（sign 组装结果）:");
        System.out.println("  url: " + lastUrl);
        System.out.println("  签名头:");
        for (Map.Entry<String, String> e : lastHeaders.entrySet()) {
            String v = e.getValue() == null ? "" : e.getValue();
            System.out.println("    " + e.getKey() + ": "
                    + (v.length() > 80 ? v.substring(0, 80) + "…(" + v.length() + "字符)" : v));
        }
        System.out.println("  body: " + (lastBody == null || lastBody.isEmpty() ? "(空)"
                : (lastBody.length() > 200 ? lastBody.substring(0, 200) + "…" : lastBody)));
    }

    /**
     * 组装并直发，返回小红书响应体。
     *
     * @param token   平台令牌
     * @param uniqueId 设备
     * @param host     目标域（Urls.XHS_REC / XHS_EDITH …）
     * @param path     接口路径（含 query），如 PATH_HOMEFEED
     * @param method   GET / POST
     * @param body     POST 请求体原文，GET 传空串
     */
    public static String signAndSend(String token, String uniqueId, String host,
                                     String path, String method, String body) {
        JSONObject req = new JSONObject()
                .put("uniqueId", uniqueId)
                .put("method", method)
                .put("path", path);
        JSONObject biz = new JSONObject().put("host", host);
        if (body != null && !body.isEmpty()) {
            biz.put("body", body);
        }
        req.put("bizParams", biz);

        JSONObject sign = Http.post(Urls.DEVICE_SIGN, req.toString(), token);
        String url = sign.optString("url");
        assert !url.isEmpty() : "签名未返回 url";

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
        return Http.sendSigned(url, headers, reqBody);
    }
}