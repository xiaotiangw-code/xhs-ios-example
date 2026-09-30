# xhs-ios-examples

调用 **xhs-ios-sign-app** 设备托管接口的示例工程。每个示例一个包、一个可直接 `main` 运行的类，
统一继承 `common/AbstractExample` 骨架——示例类只写「你要填的参数」+ 三个业务差异点
（请求 URL / 请求参数 / Content-Type），其余全部由骨架代跑。

---

## 一、目录结构

```
xhs-ios-examples/
├── pom.xml                                   # Maven 配置
├── mvnw / mvnw.cmd / .mvn/wrapper/           # Maven Wrapper
├── src/main/resources/config.properties      # 全局配置（平台账号/代理/后端地址，见 Config）
└── src/main/java/com/xhs/example/
    ├── common/                               # 公共层
    │   ├── AbstractExample.java              #   示例统一骨架：run() 代跑 登录→设备→签名→出参
    │   ├── Config.java                       #   config.properties 加载（一次加载，全局共用）
    │   ├── Urls.java                         #   URL 常量
    │   ├── Http.java                         #   HTTP 封装：JSON 交互 + 签名请求直发
    │   ├── PlatformAuth.java                 #   平台登录换 Bearer token
    │   ├── DeviceOps.java                    #   设备创建(复用) / 注册 / 创建并注册
    │   ├── Sign.java                         #   组装签名请求 → 发往小红书并取回响应
    │   ├── Out.java                          #   出参结构化打印
    │   └── ApiCrypto.java                    #   AES-GCM 传输加解密（与 sign-app 对应）
    │
    ├── loginGetToken/        例1  平台登录获取 token
    ├── passwordLogin/        例2  小红书账号密码登录
    ├── smsCodeLogin/         例3  发送验证码 + 验证码登录
    ├── quickLogin/           例4  快捷登录（免密免码）
    ├── registerDevice/       例5  注册设备
    ├── signRequest/          例6  签名并取回响应
    ├── logout/               例7  登出账号
    ├── guestHomefeed/        例8  游客请求首页
    ├── guestComments/        例9  游客请求评论页
    ├── noteDetailPreload/    例10 笔记详情预加载（detailfeed/preload）
    ├── editProxy/            例13 按设备 ID 修改出站代理（/api/device/edit；空串=清除）
    ├── follow/               例11 关注（直播间内关注主播）
    └── sendComment/          例12 直播间发送评论/口令
```

---

## 二、示例骨架：怎么写 / 怎么跑一个示例

业务类示例统一继承 `AbstractExample`，以「关注主播」为例：

```java
public class FollowExample extends AbstractExample {

    // ① 你要填的参数（只改这里）
    static final String HOST_ID = "your-host-id";   // 主播 id
    static final String ROOM_ID = "your-room-id";   // 直播间 id

    public static void main(String[] args) throws Exception {
        new FollowExample().run();                  // 一行跑完：登录→设备→签名→出参
    }

    // ② 业务差异点（一般不用改）
    protected String requestUrl()   { return Urls.XHS_EDITH + Urls.PATH_FOLLOW; }
    protected String requestParams(){ return "live_source=money&room_id=" + enc(ROOM_ID) + "..."; }
    protected String contentType()  { return "application/x-www-form-urlencoded; charset=utf-8"; }
}
```

骨架 `run()` 统一执行：平台登录 → 设备准备 → 签名直发 → 出参打印 → 业务码校验
（非 0 直接抛错，不会假成功）。GET 示例只需再覆写 `method()` 返回 `"GET"`；
`requestParams()`/`contentType()` 父类有默认实现（GET 场景免写）。

---

## 三、如何编译

```bash
cd releases/xhs-ios-examples

./mvnw compile     # 首次联网下载依赖；之后可加 -o 离线编译
```

---

## 四、如何改参数

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

## 五、如何运行

```bash
# 例1 平台登录获取 token（先跑这个，确认链路通）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.loginGetToken.LoginGetTokenExample

# 例2 账号密码登录
./mvnw exec:java -Dexec.mainClass=com.xhs.example.passwordLogin.PasswordLoginExample

# 例3 发送验证码 + 验证码登录（会提示输入收到的验证码）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.smsCodeLogin.SmsCodeLoginExample

# 例4 快捷登录
./mvnw exec:java -Dexec.mainClass=com.xhs.example.quickLogin.QuickLoginExample

# 例5 注册设备（打印新 uniqueId，可回填各登录类示例的 DEVICE_ID 复用）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.registerDevice.RegisterDeviceExample

# 例6 签名并直发（游客首页）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.signRequest.SignRequestExample

# 例7 登出账号
./mvnw exec:java -Dexec.mainClass=com.xhs.example.logout.LogoutExample

# 例8 游客请求首页（自动现场注册一台游客设备）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.guestHomefeed.GuestHomefeedExample

# 例9 游客请求评论页
./mvnw exec:java -Dexec.mainClass=com.xhs.example.guestComments.GuestCommentsExample

# 例10 笔记详情预加载（先取首页 feed，批量 preload 笔记详情）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.noteDetailPreload.NoteDetailPreloadExample

# 例11 关注（直播间内关注主播；需设备已登录 + 改 FollowExample 里的 HOST_ID/ROOM_ID）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.follow.FollowExample

# 例12 直播间发送评论/口令（需设备已登录 + 改 SendCommentExample 里的直播间参数）
./mvnw exec:java -Dexec.mainClass=com.xhs.example.sendComment.SendCommentExample
```

> 仅例3 需要键盘输入验证码（手机会收到短信），其余全部按 `config.properties` +
> 各示例类「① 你要填的参数」自动运行。

---

## 六、常见问题

| 现象 | 处理 |
|---|---|
| 编译报 `Cannot access … in offline mode` | 首次编译去掉 `-o`，联网下载依赖 |
| `HTTP 401 用户名或密码错误` | `config.properties` 的 `platform.*` 账户没改成实际可用的 |
| 注册报代理相关错误 | 在 `config.properties` 的 `proxy` 填出站代理后再跑 |
