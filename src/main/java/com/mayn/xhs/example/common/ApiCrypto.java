package com.mayn.xhs.example.common;

import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 客户端↔后端应用层加密（AES-256-GCM，与服务端 xhs.api-crypto.key 同密钥）。
 * 线上形态：请求体/响应体 = {@code {"v":1,"payload":"<base64(iv[12]||ciphertext||tag)>"}}；
 * GET 请求体为空，加 {@code X-Api-Crypto: 1} 头声明"响应可加密"。
 * 密钥编译在客户端内——防链路窃听，不防客户端逆向。
 */
public final class ApiCrypto {

    /** 协议版本号（与服务端 ApiCrypto.VERSION 一致）。 */
    public static final int VERSION = 1;
    /** 预共享密钥（32B hex；与服务端 xhs.api-crypto.key 一致，轮换时两侧同步）。 */
    private static final String KEY_HEX =
            "cae3dfa984e1d28a99d4c3147a85d0f53eec30bd5dfd8e513e7c834a379acc38";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final byte[] KEY = hex(KEY_HEX);

    private ApiCrypto() {
    }

    private static byte[] hex(String h) {
        byte[] k = new byte[h.length() / 2];
        for (int i = 0; i < k.length; i++) {
            k[i] = (byte) Integer.parseInt(h.substring(i * 2, i * 2 + 2), 16);
        }
        return k;
    }

    /** 明文 JSON → {"v":1,"payload":"<b64>"} 传输体。 */
    public static String wrap(String plaintext) throws Exception {
        JSONObject o = new JSONObject();
        o.put("v", VERSION);
        o.put("payload", encrypt(plaintext));
        return o.toString();
    }

    private static String encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[12];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        return Base64.getEncoder().encodeToString(out);
    }

    /**
     * 响应体自适配：加密形态（{"v":1,"payload":..}）解密还原明文；明文形态（过渡期/错误响应）原样返回。
     */
    public static String unwrapIfNeeded(String text) {
        if (text == null) {
            return text;
        }
        String t = text.trim();
        if (!t.startsWith("{\"v\":") || !t.contains("\"payload\"")) {
            return text;
        }
        try {
            JSONObject o = new JSONObject(t);
            if (o.optInt("v", -1) != VERSION) {
                return text;
            }
            return decrypt(o.getString("payload"));
        } catch (Exception e) {
            return text;
        }
    }

    private static String decrypt(String payloadB64) throws Exception {
        byte[] all = Base64.getDecoder().decode(payloadB64);
        if (all.length <= 12) {
            throw new IllegalArgumentException("payload 过短");
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"),
                new GCMParameterSpec(128, all, 0, 12));
        return new String(cipher.doFinal(all, 12, all.length - 12), StandardCharsets.UTF_8);
    }
}
