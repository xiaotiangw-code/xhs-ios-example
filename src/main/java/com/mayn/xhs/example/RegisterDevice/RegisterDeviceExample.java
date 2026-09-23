package com.mayn.xhs.example.RegisterDevice;

import com.mayn.xhs.example.common.DemoConfig;
import com.mayn.xhs.example.common.DeviceOps;
import com.mayn.xhs.example.common.PlatformAuth;
import com.mayn.xhs.example.common.Out;

/**
 * 例 5：注册设备（完整注册链）。
 * <p>先创建一台设备，再注册（cfg→update_device→uginfo→activate→register→profile→prb→…），
 * 成功后 phase→REGISTERED 供后续登录 / 签名使用。注册需出站，代理见 {@link DemoConfig#PROXY}。</p>
 */
public class RegisterDeviceExample {

    public static void main(String[] args) {
        String token = PlatformAuth.login();

        // 创建并注册一台设备
        String uniqueId = DeviceOps.createAndRegister(token, null, DemoConfig.PROXY);
        // 出参：注册接口返回 phase（服务端不回传设备材料）
        Out.kv("设备注册", "uniqueId", uniqueId, "phase", "REGISTERED", "代理", DemoConfig.PROXY);
        System.out.println("[ok] 设备注册成功 uniqueId=" + uniqueId + " phase=REGISTERED");
    }
}