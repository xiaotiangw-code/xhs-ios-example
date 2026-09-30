package com.xhs.example.editProxy;

import com.xhs.example.common.*;

/**
 * 例 13：按设备 ID 修改出站代理。
 *
 * <p>代理是设备属性：修改后该设备后续的发码/登录/签名代发全部走新代理
 * （与注册出口保持 IP 一致是风控要求；国际号 rnote 域需用对 rnote 可达的出口，
 * 可先 {@code curl -x <proxy> https://edith.rnote.com/} 探测，返回 200/302 即可用）。</p>
 */
public class EditProxyExample {

    static final String uniqueId = "your-device-unique-id";   // 已注册设备 uniqueId（必填）

    public static void main(String[] args) {
        String token = PlatformAuth.login();
        String proxy = Config.get("proxy", "");

        boolean hasProxy = DeviceOps.editProxy(token, uniqueId, proxy);
        // 出参：编辑接口返回 hasProxy（true=代理已生效，false=已清除改直连）
        Out.kv("修改设备代理", "uniqueId", uniqueId,
                "代理", hasProxy ? "已更新：" + proxy : "已清除（直连）");
        System.out.println("[ok] 代理已更新 uniqueId=" + uniqueId
                + " → " + (hasProxy ? proxy : "(直连)"));
    }
}
