# Nacos 本地测试指南

## 1. 安装并启动 Nacos

### 方式1：使用 Docker（推荐）

```bash
# 拉取 Nacos 镜像
docker pull nacos/nacos-server:v2.2.0

# 启动 Nacos（单机模式）
docker run -d \
  --name nacos \
  -p 8848:8848 \
  -p 9848:9848 \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=hostname \
  nacos/nacos-server:v2.2.0
```

### 方式2：下载并启动（本地）

```bash
# 1. 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.0/nacos-server-2.2.0.tar.gz

# 2. 解压
tar -xzf nacos-server-2.2.0.tar.gz
cd nacos/bin

# 3. 启动（Linux/Mac）
sh startup.sh -m standalone

# Windows
startup.cmd -m standalone
```

### 验证 Nacos 启动

访问：http://localhost:8848/nacos

- 用户名：`nacos`
- 密码：`nacos`

## 2. 在 Nacos 中创建配置

### 登录 Nacos 控制台

1. 访问 http://localhost:8848/nacos
2. 登录（用户名/密码：nacos/nacos）
3. 点击左侧菜单：**配置管理** → **配置列表**

### 创建配置

点击 **+** 按钮，创建新配置：

- **Data ID**: `dynamic-tp-test-dtp-dev.yml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**：

```yaml
spring:
  dynamic:
    tp:
      enabled: true
      enabledBanner: true
      enabledCollect: true
      collectorType: micrometer
      monitorInterval: 5
      nacos:
        dataId: dynamic-tp-test-dtp-dev.yml
        group: DEFAULT_GROUP
      configType: yml
      executors:
        - threadPoolName: test-pool-1
          corePoolSize: 2
          maximumPoolSize: 4
          keepAliveTime: 30
          queueType: VariableLinkedBlockingQueue
          queueCapacity: 1000
          rejectedHandlerType: CallerRunsPolicy
          threadNamePrefix: test-pool-1
```

点击 **发布** 保存配置。

## 3. 运行测试项目

### 启动应用

```bash
cd my-debug-module
mvn spring-boot:run
```

或者直接运行 `NacosTestApplication.main()` 方法。

### 访问测试接口

1. **查看线程池状态**：
   ```
   http://localhost:8080/status
   ```

2. **提交测试任务**：
   ```
   http://localhost:8080/test
   ```

## 4. 测试动态刷新

### 步骤1：查看初始状态

访问 http://localhost:8080/status，记录当前的：
- 核心线程数
- 最大线程数
- 队列容量

### 步骤2：在 Nacos 控制台修改配置

1. 登录 Nacos 控制台
2. 找到配置：`dynamic-tp-test-dtp-dev.yml`
3. 点击 **编辑**
4. 修改参数，例如：
   ```yaml
   executors:
     - threadPoolName: test-pool-1
       corePoolSize: 5        # 从 2 改为 5
       maximumPoolSize: 10    # 从 4 改为 10
       queueCapacity: 2000    # 从 1000 改为 2000
   ```
5. 点击 **发布**

### 步骤3：观察动态刷新

**查看应用日志**，你会看到类似这样的输出：

```
2026-01-13 16:30:15.123 INFO  com.dtp.core.DtpRegistry - DynamicTp refresher, add listener success, dataId: dynamic-tp-test-dtp-dev.yml, group: DEFAULT_GROUP
2026-01-13 16:30:20.456 INFO  com.dtp.core.DtpRegistry - DynamicTp [test-pool-1] refreshed end, changed keys: [corePoolSize, maxPoolSize, queueCapacity], corePoolSize: [2 => 5], maxPoolSize: [4 => 10], queueCapacity: [1000 => 2000]
```

**关键日志说明**：
- `changed keys`: 显示哪些参数发生了变化
- `corePoolSize: [2 => 5]`: 核心线程数从 2 变为 5
- `maxPoolSize: [4 => 10]`: 最大线程数从 4 变为 10
- `queueCapacity: [1000 => 2000]`: 队列容量从 1000 变为 2000

### 步骤4：验证参数已生效

再次访问 http://localhost:8080/status，你会看到参数已经更新！

**无需重启应用，参数立即生效！** 🎉

## 5. 测试场景

### 场景1：动态扩容

1. 初始配置：`corePoolSize: 2, maximumPoolSize: 4`
2. 在 Nacos 修改为：`corePoolSize: 10, maximumPoolSize: 20`
3. 观察日志中的刷新信息
4. 访问 `/status` 验证参数已更新

### 场景2：动态缩容

1. 当前配置：`corePoolSize: 10, maximumPoolSize: 20`
2. 在 Nacos 修改为：`corePoolSize: 5, maximumPoolSize: 10`
3. 观察日志中的刷新信息
4. 访问 `/status` 验证参数已更新

### 场景3：修改队列容量

1. 当前配置：`queueCapacity: 1000`
2. 在 Nacos 修改为：`queueCapacity: 5000`
3. 观察日志中的刷新信息
4. 访问 `/status` 验证参数已更新

## 6. 常见问题

### Q1: Nacos 连接失败

**错误**：`com.alibaba.nacos.api.exception.NacosException: endpoint is blank`

**解决**：
- 确保 Nacos 已启动：访问 http://localhost:8848/nacos
- 检查 `application.yml` 中的 `nacos.config.server-addr` 配置

### Q2: 配置不生效

**检查**：
1. Nacos 中的 Data ID 是否与 `application.yml` 中的 `data-ids` 一致
2. Group 是否一致（默认 `DEFAULT_GROUP`）
3. 配置格式是否为 YAML
4. 查看应用日志是否有监听器注册成功的日志

### Q3: 找不到线程池

**错误**：`Cannot find a specified DynamicTp, name: test-pool-1`

**解决**：
- 确保 Nacos 配置中的 `threadPoolName` 为 `test-pool-1`
- 检查配置是否正确发布到 Nacos

## 7. 完整测试流程总结

```
1. 启动 Nacos（Docker 或本地）
   ↓
2. 在 Nacos 控制台创建配置
   ↓
3. 启动测试应用
   ↓
4. 访问 /status 查看初始状态
   ↓
5. 在 Nacos 控制台修改配置
   ↓
6. 观察应用日志中的刷新信息
   ↓
7. 访问 /status 验证参数已更新
   ↓
8. 无需重启，参数立即生效！✅
```

## 8. 关键代码位置

- **监听器注册**：`NacosRefresher.afterPropertiesSet()` - 第 43-56 行
- **配置变化回调**：`NacosRefresher.receiveConfigInfo()` - 第 64-66 行
- **刷新执行**：`DtpRegistry.refresh()` - 第 91-108 行
- **参数更新**：`DtpRegistry.doRefresh()` - 第 151-198 行

现在你可以实际体验"动态"的真正威力了！🚀
