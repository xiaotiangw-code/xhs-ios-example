package com.xhs.example.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 统一配置加载：从 classpath 的 {@code config.properties} 加载<b>一次</b>，
 * 提供 {@link #get(String, String)} 供全局读取。
 *
 * <p>所有可变 / 敏感参数（平台账号、小红书手机号密码、代理、后端地址、业务参数）
 * 都集中在 {@code src/main/resources/config.properties}，改配置即可，不用动代码。</p>
 *
 * <p>★ 必须用 UTF-8 读取：{@code Properties.load(InputStream)} 默认按 ISO-8859-1 解码，
 * 配置文件里的中文会乱码，这里显式用 UTF-8 Reader 加载。</p>
 */
public final class Config {

    private static final Properties PROPS = load();

    private Config() {
    }

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                // ★ UTF-8 显式解码，否则中文配置/注释乱码
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
            // 配置文件缺失时按默认值兜底，不阻断运行
        }
        return p;
    }

    /** 读取配置项；缺失或为空时返回 {@code def} 默认值。 */
    public static String get(String key, String def) {
        String v = PROPS.getProperty(key);
        return v == null || v.trim().isEmpty() ? def : v.trim();
    }

    /** 读取配置项（无默认值版本，缺失返回 null）。 */
    public static String get(String key) {
        return PROPS.getProperty(key);
    }
}
