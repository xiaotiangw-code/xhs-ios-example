package com.mayn.xhs.example.common;

/**
 * 测试 demo 参数（写死，按需改这里）。
 * <p>与 {@link PlatformAuth} 同思路：demo 不读系统属性，统一收敛到一个常量类，
 * 每个示例直接引用。真机对接时改这些值即可。</p>
 */
public final class DemoConfig {

    /** 目标设备 uniqueId：需先在 sign-app 创建并注册（例5 / 例8 可自动建）。 */
    public static final String DEVICE_ID = "your-device-unique-id";

    /** 小红书手机号（验证码登录用）。 */
    public static final String PHONE = "your-phone";

    /** 小红书账号密码。 */
    public static final String PASSWORD = "your-password";

    /** 出站代理 user:pass@host:port（sign-app proxy-free=false 时注册/登录必填；直连环境留空）。 */
    public static final String PROXY = "";

    /** 目标笔记 id（例9 评论页用）。 */
    public static final String NOTE_ID = "your-note-id";

    private DemoConfig() {
    }
}