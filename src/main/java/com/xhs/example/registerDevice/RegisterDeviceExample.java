package com.xhs.example.registerDevice;

import com.xhs.example.common.Config;
import com.xhs.example.common.DeviceOps;
import com.xhs.example.common.PlatformAuth;
import com.xhs.example.common.Out;

/**
 * 例 5：注册设备（完整注册链）。
 *
 * <p><b>你要填的参数</b>——本示例无需业务参数（自动创建一台新设备）。
 * 公共参数（平台账号 / 出站代理）在 {@code config.properties}。</p>
 *
 * <p>先创建一台设备，再注册（cfg→update_device→uginfo→activate→register→profile→prb→…），
 * 成功后 phase→REGISTERED 供后续登录 / 签名使用。注册需出站，走 config.properties 的 proxy。</p>
 */
public class RegisterDeviceExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();
        String proxy = Config.get("proxy", "");

        // 创建并注册一台设备
        String uniqueId = DeviceOps.createAndRegister(token, null, proxy);
        // 出参：注册接口返回 phase（服务端不回传设备材料）
        Out.kv("设备注册", "uniqueId", uniqueId, "phase", "REGISTERED",
                "代理", proxy.isEmpty() ? "(直连)" : "已配置");
        System.out.println("[ok] 设备注册成功 uniqueId=" + uniqueId + " phase=REGISTERED");
    }
}