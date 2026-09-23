package com.mayn.xhs.example.common;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 极简 HTTP 封装：JSON {@code {code,msg,data}} 交互 + 原生小杨请求直发。
 * <p>sign-app 统一返回 {@code {code,msg,data}}（code 与 HTTP 状态码一致）。
 * 所有请求失败抛 {@link AssertionError}，带后端 msg，配合断言表达"必须成功"。</p>
 */
public final class Http {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private Http() {
    }

    /** 平台鉴权请求（无需 token）。返回解析出的 data 对象。 */
    public static JSONObject postNoAuth(String url, String jsonBody) {
        return post(url, jsonBody, null);
    }

    /** 带平台 Bearer token 的 POST，返回 data 对象。 */
    public static JSONObject post(String url, String jsonBody, String bearerToken) {
        return request(method(url, jsonBody, bearerToken));
    }

    /** 平台 GET，返回 data 对象。 */
    public static JSONObject get(String url, String bearerToken) {
        Request.Builder b = new Request.Builder().url(url);
        b.header("X-Api-Crypto", "1");   // GET 无请求体：声明"能理解加密响应"
        if (bearerToken != null) {
            b.header("Authorization", "Bearer " + bearerToken);
        }
        return request(b.build());
    }

    /** 把 sign 下发的完整请求（URL + headers + body）直发小红书目标域，返回原始响应体。 */
    public static String sendSigned(String url, Map<String, String> headers, String body) {
        Request.Builder b = new Request.Builder().url(url);
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (!"Host".equalsIgnoreCase(e.getKey())) {
                b.header(e.getKey(), e.getValue());
            }
        }
        if (body != null && !body.isEmpty()) {
            b.post(RequestBody.create(body, JSON));
        }
        try (Response r = CLIENT.newCall(b.build()).execute()) {
            String text = r.body() == null ? "" : r.body().string();
            if (r.code() < 200 || r.code() >= 300) {
                throw new AssertionError("小红书直发 HTTP " + r.code() + ": " + text);
            }
            return text;
        } catch (IOException e) {
            throw new AssertionError("小红书直发失败: " + e.getMessage(), e);
        }
    }

    /** 发请求并断言 sign-app 层成功，返回 data。 */
    private static JSONObject request(Request request) {
        try (Response r = CLIENT.newCall(request).execute()) {
            // 应用层加密：加密形态响应解密还原；明文形态（过渡期/错误响应）原样
            String text = ApiCrypto.unwrapIfNeeded(r.body() == null ? "" : r.body().string());
            JSONObject json = new JSONObject(text);
            int code = json.optInt("code", -1);
            if (code != 200) {
                throw new AssertionError("HTTP " + r.code() + " code=" + code + " msg="
                        + json.optString("msg") + " url=" + request.url());
            }
            return json.optJSONObject("data");
        } catch (IOException e) {
            throw new AssertionError("请求失败: " + e.getMessage(), e);
        }
    }

    private static Request method(String url, String jsonBody, String bearerToken) {
        // 请求体应用层加密（★ 默认打开，无开关）
        String bodyText;
        try {
            bodyText = ApiCrypto.wrap(jsonBody);
        } catch (Exception e) {
            throw new AssertionError("请求加密失败", e);
        }
        Request.Builder b = new Request.Builder()
                .url(url)
                .post(RequestBody.create(bodyText, JSON));
        b.header("X-Api-Crypto", "1");
        if (bearerToken != null) {
            b.header("Authorization", "Bearer " + bearerToken);
        }
        return b.build();
    }
}