package com.xhs.example.common;

/**
 * 后端 / 小红书目标站 URL 常量。
 * <p>sign-app 设备托管 API 为本地服务；小红书目标域由各示例按需选用。</p>
 */
public final class Urls {

    /** sign-app 后端根地址（见 config.properties 的 backend.base，按实际运行端口调整）。 */
    public static final String SIGN_APP_BASE = Config.get("backend.base", "http://127.0.0.1:8081");

    // ---- sign-app 平台鉴权 ----
    public static final String AUTH_LOGIN = SIGN_APP_BASE + "/api/auth/login";
    public static final String AUTH_REFRESH = SIGN_APP_BASE + "/api/auth/refresh";

    // ---- sign-app 设备托管 ----
    public static final String DEVICE_CREATE = SIGN_APP_BASE + "/api/device/create";
    public static final String DEVICE_LIST = SIGN_APP_BASE + "/api/device/list";
    public static final String DEVICE_BY_PHONE = SIGN_APP_BASE + "/api/device/by-phone";
    public static final String DEVICE_REGISTER = SIGN_APP_BASE + "/api/device/register";
    public static final String DEVICE_EDIT = SIGN_APP_BASE + "/api/device/edit";
    public static final String DEVICE_SEND = SIGN_APP_BASE + "/api/device/send";
    public static final String DEVICE_LOGOUT = SIGN_APP_BASE + "/api/device/logout";
    public static final String DEVICE_DELETE = SIGN_APP_BASE + "/api/device/delete";
    public static final String DEVICE_LOGIN_PASSWORD = SIGN_APP_BASE + "/api/device/login/password";
    public static final String DEVICE_LOGIN_CODE = SIGN_APP_BASE + "/api/device/login/code";
    public static final String DEVICE_LOGIN_VFC = SIGN_APP_BASE + "/api/device/login/vfc-code";
    public static final String DEVICE_LOGIN_QUICK = SIGN_APP_BASE + "/api/device/login/quick";

    // ---- 小红书目标站（签名直发的 Host）----
    public static final String XHS_REC = "https://rec.xiaohongshu.com";       // 游客首页 / 内容
    public static final String XHS_EDITH = "https://edith.xiaohongshu.com";   // 登录态 / 评论 / 关系
    public static final String XHS_WWW = "https://www.xiaohongshu.com";       // 网页域
    public static final String XHS_LIVE_ROOM = "https://live-room.xiaohongshu.com"; // 直播间会话域

    // ---- 小红书接口路径 ----
    public static final String PATH_HOMEFEED = "/api/sns/v6/homefeed?client_volume=0.4125&num=20"
            + "&oid=homefeed_recommend&orientation=portait&personalization=1"
            + "&refresh_type=2&use_jpeg=1&user_action=0";
    public static final String PATH_COMMENT_LIST = "/api/sns/v5/note/comment/list";
    /** 笔记详情预加载（POST edith 域，body=form：data=[{type,id,xsec_token}…]&source=main）。 */
    public static final String PATH_PRELOAD = "/api/sns/v1/note/detailfeed/preload";
    /** 关注用户/主播（POST edith 域，form，直播间内关注 scene_point=2942）。 */
    public static final String PATH_FOLLOW = "/api/sns/v1/user/follow";
    /** 直播间发送评论/口令（POST live-room 域，form）。 */
    public static final String PATH_SEND_COMMENT = "/api/sns/v1/live/interaction/send_comment";

    private Urls() {
    }
}