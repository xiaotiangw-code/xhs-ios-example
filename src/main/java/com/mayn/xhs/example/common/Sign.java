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

    private Sign() {
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
        return Http.sendSigned(url, headers, reqBody);
    }
}