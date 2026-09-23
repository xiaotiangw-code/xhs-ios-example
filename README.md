# xhs-ios-examples

调用 **xhs-ios-sign-app** 设备托管接口的示例工程。每个示例一个包、一个可直接 `main` 运行的类，
公共逻辑收敛在 `common` 包。

---

## 一、目录结构

```
xhs-ios-examples/
├── pom.xml                                   # Maven 配置
├── mvnw / mvnw.cmd / .mvn/wrapper/           # Maven Wrapper
└── src/main/java/com/mayn/xhs/example/
    ├── common/                               # 公共层
    │   ├── Urls.java                         #   URL 常量
    │   ├── Http.java                         #   HTTP 封装：JSON 交互 + 签名请求直发
    │   ├── PlatformAuth.java                 #   平台登录换 Bearer token
    │   ├── DeviceOps.java                    #   设备创建(复用) / 注册 / 创建并注册
    │   ├── DemoConfig.java                   #   demo 参数（设备 / 手机号 / 密码 / 代理 / noteId）
    │   └── Sign.java                         #   组装签名请求 → 直发小红书
    │
    ├── LoginGetToken/   例1  平台登录获取 token
    ├── PasswordLogin/   例2  小红书账号密码登录
    ├── SmsCodeLogin/    例3  发送验证码 + 验证码登录
    ├── QuickLogin/      例4  快捷登录（免密免码）
    ├── RegisterDevice/  例5  注册设备
    ├── SignRequest/     例6  签名并直发（游客首页）
    ├── Logout/          例7  登出账号
    ├── GuestHomefeed/   例8  游客请求首页
    └── GuestComments/   例9  游客请求评论页
```

---

## 二、如何编译

```bash
cd releases/xhs-ios-examples

./mvnw compile     # 首次联网下载依赖；之后可加 -o 离线编译
```

---

## 三、如何改参数

参数集中写死在三个公共类，改一处即可：

### 1. 平台账户（`common/PlatformAuth.java`）
```java
public static final String USERNAME     = "your-username";
public static final String PASSWORD     = "your-password";
public static final String MACHINE_CODE = "your-machine-code";
```

### 2. 后端地址（`common/Urls.java`）
```java
public static final String SIGN_APP_BASE = "http://127.0.0.1:8081";
```

### 3. 业务参数（`common/DemoConfig.java`）
```java
public static final String DEVICE_ID = "your-device-unique-id";   // 已注册设备
public static final String PHONE     = "your-phone";                // 小红书手机号
public static final String PASSWORD  = "your-password";           // 小红书密码
public static final String PROXY     = "";                        // 出站代理 user:pass@host:port
public static final String NOTE_ID   = "your-note-id";            // 评论页目标笔记
```

---

## 四、如何运行

```bash
# 例1 平台登录获取 token（先跑这个，确认链路通）
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.LoginGetToken.LoginGetTokenExample

# 例2 账号密码登录
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.PasswordLogin.PasswordLoginExample

# 例3 发送验证码 + 验证码登录（会提示输入收到的验证码）
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.SmsCodeLogin.SmsCodeLoginExample

# 例4 快捷登录
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.QuickLogin.QuickLoginExample

# 例5 注册设备（打印新 uniqueId，可回填 DemoConfig.DEVICE_ID 复用）
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.RegisterDevice.RegisterDeviceExample

# 例6 签名并直发（游客首页）
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.SignRequest.SignRequestExample

# 例7 登出账号
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.Logout.LogoutExample

# 例8 游客请求首页（DEVICE_ID 为空时自动现场注册一台）
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.GuestHomefeed.GuestHomefeedExample

# 例9 游客请求评论页
./mvnw exec:java -Dexec.mainClass=com.mayn.xhs.example.GuestComments.GuestCommentsExample
```

> 仅例3 需要键盘输入验证码（手机会收到短信），其余全部按 `DemoConfig` 里的参数自动运行。

---

## 五、常见问题

| 现象 | 处理 |
|---|---|
| 编译报 `Cannot access … in offline mode` | 首次编译去掉 `-o`，联网下载依赖 |
| `HTTP 401 用户名或密码错误` | `PlatformAuth` 账户没改成实际可用的 |
| 注册报代理相关错误 | 在 `DemoConfig.PROXY` 填出站代理后再跑 |
