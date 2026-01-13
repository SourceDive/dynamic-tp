# Nacos 配置格式说明

## 问题：修改配置后没有生效

如果修改了 Nacos 配置，但 `/status` 接口显示没有变化，请检查以下几点：

## 1. 配置格式必须正确

### ✅ 正确的配置格式（YAML）

```yaml
spring:
  dynamic:
    tp:
      executors:
        - threadPoolName: test-pool-1
          corePoolSize: 5          # 核心线程数
          maximumPoolSize: 10       # 最大线程数
          keepAliveTime: 30         # 线程存活时间（秒）
          queueType: VariableLinkedBlockingQueue
          queueCapacity: 2000       # 队列容量
          rejectedHandlerType: CallerRunsPolicy
          threadNamePrefix: test-pool-1
```

### ❌ 错误的配置格式

```yaml
# 错误1：缺少 spring.dynamic.tp 前缀
executors:
  - threadPoolName: test-pool-1
    corePoolSize: 5

# 错误2：格式不对
test-pool-1:
  corePoolSize: 5
```

## 2. 配置标识必须匹配

- **Data ID**: `dynamic-tp-test-dtp-dev.yml`（必须完全匹配）
- **Group**: `DEFAULT_GROUP`（必须完全匹配）
- **配置格式**: `YAML`（不是 Properties）

## 3. 检查监听器是否工作

启动应用后，查看日志应该看到：

```
✅ DynamicTp refresher, add listener success, dataId: dynamic-tp-test-dtp-dev.yml, group: DEFAULT_GROUP
✅ 监听器已注册！现在在 Nacos 控制台修改配置，会自动触发 receiveConfigInfo() 方法
```

修改配置后，应该看到：

```
🔥 收到 Nacos 配置变更通知！
配置内容长度: xxx
配置类型: YML
开始刷新线程池参数...
✅ 线程池参数刷新完成！
```

**如果没有看到这些日志，说明监听器没有工作！**

## 4. 常见问题排查

### 问题1：监听器没有触发

**原因**：
- Nacos 客户端连接失败（检查是否有 `Client not connected` 错误）
- Data ID 或 Group 不匹配
- 配置格式错误

**解决**：
1. 检查应用日志中的错误信息
2. 确认 Data ID 和 Group 完全匹配
3. 访问 `/check-listener` 接口查看状态

### 问题2：配置格式错误

**原因**：
- 配置缺少 `spring.dynamic.tp` 前缀
- 配置格式不是 YAML
- 线程池名称不匹配

**解决**：
- 使用上面提供的正确格式
- 确保 `threadPoolName` 为 `test-pool-1`

### 问题3：参数没有更新

**原因**：
- 配置解析失败
- 参数值无效（如 corePoolSize > maximumPoolSize）

**解决**：
- 查看日志中的错误信息
- 确保参数值合理

## 5. 测试步骤

1. **启动应用**，查看日志确认监听器已注册
2. **访问** `http://localhost:8080/status` 查看当前参数
3. **在 Nacos 控制台修改配置**（如将 corePoolSize 从 2 改为 5）
4. **查看应用日志**，应该看到刷新日志
5. **再次访问** `/status`，参数应该已更新

## 6. 调试接口

访问 `http://localhost:8080/check-listener` 可以查看监听器状态和配置要求。
