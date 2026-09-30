package com.xhs.example.common;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 极简 HTTP 封装：JSON {@code {code,msg,data}} 交互 + 签名请求直发。
 * <p>sign-app 统一返回 {@code {code,msg,data}}（code 与 HTTP 状态码一致）。
 * 所有请求失败抛 {@link AssertionError}，带后端 msg，配合断言表达"必须成功"。</p>
 */
public final class Http {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
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

    /**
     * 把 sign 下发的完整请求（URL + headers + body）直发小红书目标域，返回原始响应体。
     *
     * <p>★ Content-Type 以签名头里的为准（form 表单接口若按 JSON 发，服务端解析不到
     * body 字段直接 500）；代理非空则经该代理出站（sign 回填的设备代理）；无代理头时
     * 走直连。</p>
     */
    public static String sendSigned(String url, Map<String, String> headers, String body, String proxy) {
        Request.Builder b = new Request.Builder().url(url);
        String contentType = null;
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if ("Host".equalsIgnoreCase(e.getKey()) || e.getValue() == null) {
                continue;
            }
            b.header(e.getKey(), e.getValue());
            if ("Content-Type".equalsIgnoreCase(e.getKey())) {
                contentType = e.getValue();
            }
        }
        if (body != null && !body.isEmpty()) {
            MediaType mediaType = contentType == null ? JSON : MediaType.parse(contentType);
            b.post(RequestBody.create(body, mediaType));
        }
        OkHttpClient client = (proxy == null || proxy.isEmpty()) ? CLIENT : proxiedClient(proxy);
        try (Response r = client.newCall(b.build()).execute()) {
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

    /**
     * 按代理串构建带代理的 OkHttp 客户端。
     * <p>支持 host:port / user:pass@host:port / scheme://user:pass@host:port（socks5、http）。</p>
     */
    private static OkHttpClient proxiedClient(String proxyText) {
        String scheme = "socks";
        String rest = proxyText;
        int schemeIdx = proxyText.indexOf("://");
        if (schemeIdx > 0) {
            scheme = proxyText.substring(0, schemeIdx);
            rest = rest.substring(schemeIdx + 3);
        }
        String userInfo = null;
        int at = rest.lastIndexOf('@');
        if (at > 0) {
            userInfo = rest.substring(0, at);
            rest = rest.substring(at + 1);
        }
        String host = rest;
        int port = scheme.startsWith("socks") ? 1080 : 80;
        int colon = rest.lastIndexOf(':');
        if (colon > 0) {
            host = rest.substring(0, colon);
            try {
                port = Integer.parseInt(rest.substring(colon + 1));
            } catch (NumberFormatException ignored) {
            }
        }
        Proxy.Type type = scheme.startsWith("socks") ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
        InetSocketAddress addr = new InetSocketAddress(host, port);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .proxy(new Proxy(type, addr));
        if (userInfo != null) {
            int sep = userInfo.indexOf(':');
            final String user = sep > 0 ? userInfo.substring(0, sep) : userInfo;
            final String pass = sep > 0 ? userInfo.substring(sep + 1) : "";
            if (type == Proxy.Type.HTTP) {
                builder.proxyAuthenticator((route, response) -> response.request().newBuilder()
                        .header("Proxy-Authorization", okhttp3.Credentials.basic(user, pass))
                        .build());
            } else {
                // SOCKS 代理认证走 JVM 全局 Authenticator（demo 单线程场景可接受）
                java.net.Authenticator.setDefault(new java.net.Authenticator() {
                    @Override
                    protected java.net.PasswordAuthentication getPasswordAuthentication() {
                        return new java.net.PasswordAuthentication(user, pass.toCharArray());
                    }
                });
            }
        }
        return builder.build();
    }
}
