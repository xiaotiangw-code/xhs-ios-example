# xhs-ios-examples

调用 **xhs-ios-sign-app** 设备托管接口的示例工程。每个示例一个包、一个可直接 `main` 运行的类，
公共逻辑收敛在 `common` 包。

---

## 一、目录结构

```
xhs-ios-examples/
├── pom.xml                                   # Maven 配置
├── mvnw / mvnw.cmd / .mvn/wrapper/           # Maven Wrapper
└── src/main/java/com/xhs/example/
    ├── common/                               # 公共层
    │   ├── Urls.java                         #   URL 常量
    │   ├── Http.java                         #   HTTP 封装：JSON 交互 + 签名请求直发
    │   ├── PlatformAuth.java                 #   平台登录换 Bearer token
    │   ├── DeviceOps.java                    #   设备创建(复用) / 注册 / 创建并注册
    │   ├── DemoConfig.java                   #   demo 参数（设备 / 手机号 / 密码 / 代理 / noteId）
    │   └── Sign.java                         #   组装签名请求 →（默认）服务器代发取响应
    │
    ├── LoginGetToken/   例1  平台登录获取 token
    ├── PasswordLogin/   例2  小红书账号密码登录
    ├── SmsCodeLogin/    例3  发送验证码 + 验证码登录
    ├── QuickLogin/      例4  快捷登录（免密免码）
    ├── RegisterDevice/  例5  注册设备
    ├── SignRequest/     例6  签名并取回响应（默认服务器代发）
    ├── Logout/          例7  登出账号
    ├── GuestHomefeed/   例8  游客请求首页
    ├── GuestComments/   例9  游客请求评论页
    └── NoteDetailPreload/ 例10 笔记详情预加载（detailfeed/preload）
    ├── Follow/          例11 关注（直播间内关注主播）
    └── SendComment/     例12 直播间发送评论/口令
```

---

## 二、如何编译

```bash
cd releases/xhs-ios-examples

./mvnw compile     # 首次联网下载依赖；之后可加 -o 离线编译
```

---

## 三、如何改参数

**公共可变 / 敏感参数统一放在 `src/main/resources/config.properties`**（纯 ASCII，任何
编辑器打开不乱码；你填的值可以是中文，按 UTF-8 读取），配置类 `common/Config`
加载一次、全局共用。改配置即可，不用动代码：

```properties
# 平台账号（sign-app 后台账户，登录换 Bearer token）
platform.username=your-username
platform.password=your-password
platform.machineCode=your-machine-code

# 出站代理（proxy-free=false 时注册/登录/代发必填；直连留空）
proxy=socks5://user:pass@host:port

# 后端地址（sign-app 根地址）
backend.base=http://127.0.0.1:8081
```

> **业务 / 用例专属参数不进配置文件，跟着用例走**：小红书手机号密码在
> `PasswordLoginExample` / `SmsCodeLoginExample`，设备 uniqueId 在各登录类用例，
> 笔记 id 在 `GuestCommentsExample`，直播间参数在 `FollowExample` / `SendCommentExample`
> ——打开对应示例类，改「① 你要填的参数」区块即可。

---

## 四、如何运行

```bash
# 例1 平台登录获取 token（先跑这个，确认链路通）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.loginGetToken.LoginGetTokenExample

# 例2 账号密码登录
./mvnw exec:java -Dexec.mainClass=com.xhs.example.passwordLogin.PasswordLoginExample

# 例3 发送验证码 + 验证码登录（会提示输入收到的验证码）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.smsCodeLogin.SmsCodeLoginExample

# 例4 快捷登录
./mvnw exec:java -Dexec.mainClass=com.xhs.example.quickLogin.QuickLoginExample

# 例5 注册设备（打印新 uniqueId，可回填 DemoConfig.DEVICE_ID 复用）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.registerDevice.RegisterDeviceExample

# 例6 签名并直发（游客首页）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.signRequest.SignRequestExample

# 例7 登出账号
./mvnw exec:java -Dexec.mainClass=com.xhs.example.logout.LogoutExample

# 例8 游客请求首页（DEVICE_ID 为空时自动现场注册一台）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.guestHomefeed.GuestHomefeedExample

# 例9 游客请求评论页
./mvnw exec:java -Dexec.mainClass=com.xhs.example.guestComments.GuestCommentsExample

# 例10 笔记详情预加载（先取首页 feed，批量 preload 笔记详情）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.noteDetailPreload.NoteDetailPreloadExample

# 例11 关注（直播间内关注主播；需设备已登录 + 填 DemoConfig.ROOM_ID/HOST_ID）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.follow.FollowExample

# 例12 直播间发送评论/口令（需设备已登录 + 填 DemoConfig.ROOM_ID/COMMENT_CONTENT 等）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.sendComment.SendCommentExample
```

> 仅例3 需要键盘输入验证码（手机会收到短信），其余全部按 `DemoConfig` 里的参数自动运行。

---

## 四之二、签名请求：从服务器代发（默认开）

`common/Sign.java` 的 `SERVER_SEND` 控制「从服务器代发请求」：

```java
public static final boolean SERVER_SEND = true;   // 默认 true
```

| 取值 | 行为 | 前置条件 |
|---|---|---|
| `true`（默认） | `/api/device/sign` 带 `serverSend=true`，**sign-app 组装签名后直接发往小红书**，把真实响应回填 `serverSent/serverHttpCode/serverResponseHeaders/serverResponseBody`；示例直接取响应体 | 该设备需已配置出站代理，或服务端放开 `xhs.ios.proxy-free` 允许直连；否则返回业务提示「设备未配置代理」 |
| `false` | 只组装签名（返回 `url/headers/body`；设备配了代理时另回填 `proxy`），**由客户端自己直发**目标域 | 客户端需能出网（走响应回填的 `proxy` 或直连） |

单个调用也可显式指定：`Sign.signAndSend(token, uniqueId, host, path, method, body, false)`。

---

## 五、常见问题

| 现象 | 处理 |
|---|---|
| 编译报 `Cannot access … in offline mode` | 首次编译去掉 `-o`，联网下载依赖 |
| `HTTP 401 用户名或密码错误` | `PlatformAuth` 账户没改成实际可用的 |
| 注册报代理相关错误 | 在 `DemoConfig.PROXY` 填出站代理后再跑 |
