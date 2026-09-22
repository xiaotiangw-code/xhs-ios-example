package com.mayn.xhs.example.common;

import org.json.JSONObject;

/**
 * 设备公共操作：创建（复用）设备 + 注册设备。
 * <p>一台设备一生只注册一次；创建时带 phone 则同号设备直接复用。</p>
 */
public final class DeviceOps {

    private DeviceOps() {
    }

    /** 创建（或复用）设备，返回 uniqueId。model 可空=全池随机。 */
    public static String createOrReuse(String token, String model, String phone, String zone) {
        JSONObject body = new JSONObject();
        if (model != null) {
            body.put("model", model);
        }
        if (phone != null) {
            body.put("phone", phone);
        }
        if (zone != null) {
            body.put("zone", zone);
        }
        JSONObject data = Http.post(Urls.DEVICE_CREATE, body.toString(), token);
        String uniqueId = data.optString("uniqueId");
        assert !uniqueId.isEmpty() : "创建设备未返回 uniqueId";
        return uniqueId;
    }

    /** 注册设备（完整注册链），断言 phase 到 REGISTERED。proxy 可空。 */
    public static String register(String token, String uniqueId, String proxy) {
        JSONObject body = new JSONObject().put("uniqueId", uniqueId);
        if (proxy != null && !proxy.isEmpty()) {
            body.put("proxy", proxy);
        }
        JSONObject data = Http.post(Urls.DEVICE_REGISTER, body.toString(), token);
        String phase = data.optString("phase");
        assert "REGISTERED".equals(phase) : "注册未到 REGISTERED，实际 phase=" + phase;
        return phase;
    }

    /** 创建并注册一台设备（省事入口）。 */
    public static String createAndRegister(String token, String model, String proxy) {
        String uniqueId = createOrReuse(token, model, null, null);
        register(token, uniqueId, proxy);
        return uniqueId;
    }
}