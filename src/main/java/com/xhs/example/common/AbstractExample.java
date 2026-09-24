package com.xhs.example.common;

import org.json.JSONObject;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 示例统一骨架（模板方法）。
 *
 * <p>公共执行流程——平台登录 → 设备准备 → 签名直发 → 出参 → 断言——全部收在本类
 * {@link #run()}，子类只需实现 <b>三个业务差异点</b>：请求 URL / 请求参数 / Content-Type。
 * 新手跑某个示例 = 填这三个方法返回的东西，然后调 {@code run()}。</p>
 *
 * <p>公共参数（平台账号 / 出站代理 / 后端地址）在 {@code config.properties} 配一次全工程通用。</p>
 */
public abstract class AbstractExample {

    // ─── 子类必须实现 ───

    /** ① 请求 URL：完整地址（含域名 / 路径 / query），如 "https://edith.xiaohongshu.com/api/sns/v1/user/follow"。 */
    protected abstract String requestUrl();

    // ─── 子类可按需覆写（父类提供默认实现）───

    /** ② 请求参数：body 原文。默认空串 = GET 无 body；POST 表单类的子类覆写。 */
    protected String requestParams() {
        return "";
    }

    /** ③ Content-Type。默认 null = 无请求体（GET）；表单 / JSON 类的子类覆写。 */
    protected String contentType() {
        return null;
    }

    /** 请求方法，默认 POST，GET 请覆写。 */
    protected String method() {
        return "POST";
    }

    /** 是否需要签名设备（登录 / 注册类示例可覆写为 false）。 */
    protected boolean needDevice() {
        return true;
    }

    /** 传给 sign 的额外 bizParams（如场景点覆盖），默认 null。 */
    protected Map<String, String> extraBiz() {
        return null;
    }

    /** 请求成功后的出参展示，默认打印响应 data。 */
    protected void onSuccess(JSONObject resp, String respText) {
        Object data = resp.opt("data");
        Out.raw("响应 data", data == null ? resp.toString() : String.valueOf(data));
    }

    // ─── 公共执行流程（子类 main() 里调 run() 即可跑通整个示例）───

    public final void run() throws Exception {
        String token = PlatformAuth.login();
        String uniqueId = "";
        if (needDevice()) {
            uniqueId = DeviceOps.createAndRegister(token, null, Config.get("proxy", ""));
            System.out.println("[i] 设备 uniqueId=" + uniqueId);
        }
        String respText = send(token, uniqueId);
        JSONObject resp = new JSONObject(respText);
        onSuccess(resp, respText);
        // ★ 显式校验业务码（不用 assert：JVM 默认不启用 -ea，assert 会静默失效）
        int code = resp.optInt("code", -1);
        if (code != 0) {
            throw new IllegalStateException("业务失败 code=" + code
                    + "，响应: " + (respText.length() > 500 ? respText.substring(0, 500) + "…" : respText));
        }
        System.out.println("[ok] 请求成功 code=0");
    }

    /**
     * 组装并签名直发小红书业务接口。
     * <p>从 {@link #requestUrl()} 解析出 host + path 交给 sign-app；body / contentType /
     * method / extraBiz 一并传入。登录 / 注册等直调后端接口的示例可覆写本方法。</p>
     */
    protected String send(String token, String uniqueId) throws Exception {
        URI uri = URI.create(requestUrl());
        String host = uri.getScheme() + "://" + uri.getAuthority();
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        if (uri.getRawQuery() != null) {
            path += "?" + uri.getRawQuery();
        }
        return Sign.signAndSend(token, uniqueId, host, path, method(),
                requestParams(), contentType(), Sign.SERVER_SEND, extraBiz());
    }

    /** 表单 body 的 URL 编码工具（子类组参时用）。 */
    protected static String enc(String value) {
        try {
            return java.net.URLEncoder.encode(value == null ? "" : value, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
