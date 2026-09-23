package com.mayn.xhs.example.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 示例输出格式工具：把接口出参结构化打印到控制台，方便直接看数据。
 *
 * <p>统一格式：{@code [出参] <名称>} + 缩进 JSON；长数组只打前 {@link #MAX_ITEMS} 条，
 * 长字符串截断，避免刷屏。</p>
 */
public final class Out {

    /** 数组/对象最多打印的条目数。 */
    public static final int MAX_ITEMS = 3;
    /** 单个字符串值最大打印长度。 */
    public static final int MAX_STR = 200;

    private Out() {
    }

    /** 打印 JSON 对象出参（含 null/空处理）。 */
    public static void json(String label, JSONObject obj) {
        System.out.println("[出参] " + label + ":");
        if (obj == null) {
            System.out.println("  (null)");
            return;
        }
        print(obj, "  ", 0);
    }

    /** 打印 JSON 原文出参（自动解析为结构化；解析失败原样截断打印）。 */
    public static void raw(String label, String text) {
        System.out.println("[出参] " + label + ":");
        if (text == null || text.trim().isEmpty()) {
            System.out.println("  (空)");
            return;
        }
        String t = text.trim();
        try {
            if (t.startsWith("{")) {
                print(new JSONObject(t), "  ", 0);
                return;
            }
            if (t.startsWith("[")) {
                print(new JSONArray(t), "  ", 0);
                return;
            }
        } catch (Exception ignored) {
            // 非 JSON，原样打印
        }
        System.out.println("  " + cut(t));
    }

    /** 打一行键值摘要（适合只需要几个关键字段的场景）。 */
    public static void kv(String label, String... keyValues) {
        StringBuilder sb = new StringBuilder("[出参] ").append(label).append(": ");
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(keyValues[i]).append('=').append(cut(String.valueOf(keyValues[i + 1])));
        }
        System.out.println(sb);
    }

    /** 打印数组元素个数 + 前几条的关键字段（列表类接口用）。 */
    public static void list(String label, JSONArray arr, String... keys) {
        if (arr == null) {
            System.out.println("[出参] " + label + ": (null)");
            return;
        }
        System.out.println("[出参] " + label + ": 共 " + arr.length() + " 条"
                + (arr.length() > MAX_ITEMS ? "（下示前 " + MAX_ITEMS + " 条）" : ""));
        for (int i = 0; i < Math.min(MAX_ITEMS, arr.length()); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) {
                System.out.println("  [" + i + "] " + cut(String.valueOf(arr.opt(i))));
                continue;
            }
            StringBuilder sb = new StringBuilder("  [").append(i).append("] ");
            boolean first = true;
            for (String k : keys) {
                Object v = o.opt(k);
                if (v == null) {
                    continue;
                }
                if (!first) {
                    sb.append(" | ");
                }
                sb.append(k).append('=').append(cut(String.valueOf(v)));
                first = false;
            }
            System.out.println(sb);
        }
    }

    // ---- 递归打印 ----

    private static void print(Object node, String indent, int depth) {
        if (depth > 8) {
            System.out.println(indent + "…（层级过深已省略）");
            return;
        }
        if (node instanceof JSONObject) {
            JSONObject o = (JSONObject) node;
            if (o.length() == 0) {
                System.out.println(indent + "{}");
                return;
            }
            int shown = 0;
            for (Iterator<String> it = o.keys(); it.hasNext(); ) {
                String k = it.next();
                if (shown++ >= 40) {
                    System.out.println(indent + "…（还有 " + (o.length() - 40) + " 个字段）");
                    break;
                }
                Object v = o.opt(k);
                if (v instanceof JSONObject || v instanceof JSONArray) {
                    System.out.println(indent + k + ":");
                    print(v, indent + "  ", depth + 1);
                } else {
                    System.out.println(indent + k + ": " + cut(String.valueOf(v)));
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray a = (JSONArray) node;
            if (a.length() == 0) {
                System.out.println(indent + "[]");
                return;
            }
            int n = Math.min(MAX_ITEMS, a.length());
            for (int i = 0; i < n; i++) {
                Object v = a.opt(i);
                if (v instanceof JSONObject || v instanceof JSONArray) {
                    System.out.println(indent + "- [" + i + "]:");
                    print(v, indent + "  ", depth + 1);
                } else {
                    System.out.println(indent + "- " + cut(String.valueOf(v)));
                }
            }
            if (a.length() > n) {
                System.out.println(indent + "…（共 " + a.length() + " 条，已省略 " + (a.length() - n) + " 条）");
            }
        } else {
            System.out.println(indent + cut(String.valueOf(node)));
        }
    }

    /** 长字符串截断（保首尾可辨识）。 */
    private static String cut(String s) {
        if (s == null) {
            return "null";
        }
        String one = s.replace("\n", " ").replace("\r", "");
        return one.length() <= MAX_STR ? one : one.substring(0, MAX_STR) + "…(" + one.length() + "字符)";
    }
}
