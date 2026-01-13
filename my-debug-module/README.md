# My Debug Module

这是一个用于阅读 Dynamic-Tp 源码的调试模块。

## 使用方法

### 1. 运行调试入口

直接运行 `DebugMain` 类的 `main` 方法：

```bash
# 在 IDE 中直接运行 DebugMain.main() 方法
# 或者使用 Maven 运行
mvn exec:java -Dexec.mainClass="com.dtp.debug.DebugMain" -pl my-debug-module
```

### 2. 调试源码

在 `DebugMain.java` 中设置断点，可以跟踪以下关键流程：

- **创建线程池**: `ThreadPoolCreator.createDynamicFast()`
- **注册线程池**: `DtpRegistry.register()`
- **获取线程池**: `DtpRegistry.getExecutor()`
- **执行任务**: `executor.execute()`

### 3. 动态调整演示

运行 `DynamicAdjustDemo` 类查看动态调整的效果：

```bash
mvn exec:java -Dexec.mainClass="com.dtp.debug.DynamicAdjustDemo" -pl my-debug-module
```

**动态调整体现在：**

1. **运行时修改核心线程数** (`corePoolSize`)
   - 通过 `DtpRegistry.refresh()` 方法动态调整
   - 无需重启应用，立即生效

2. **运行时修改最大线程数** (`maximumPoolSize`)
   - 可以动态扩大或缩小线程池容量

3. **运行时修改线程存活时间** (`keepAliveTime`)
   - 调整空闲线程的存活时间

4. **运行时修改队列容量** (`queueCapacity`)
   - 仅限 `VariableLinkedBlockingQueue` 类型
   - 可以动态调整队列大小

5. **运行时修改拒绝策略** (`rejectedHandlerType`)
   - 可以动态切换拒绝策略

**动态调整流程：**
```
配置中心（Nacos/Apollo/Zookeeper）配置变更
    ↓
监听器监听到配置变化
    ↓
调用 AbstractRefresher.refresh()
    ↓
调用 DtpRegistry.refresh()
    ↓
调用 DtpRegistry.doRefresh()
    ↓
更新线程池参数（setCorePoolSize、setMaximumPoolSize 等）
    ↓
参数立即生效，影响后续任务执行
```

### 4. 阅读源码建议

建议按照以下顺序阅读源码：

1. **ThreadPoolCreator** - 线程池创建器
   - `core/src/main/java/com/dtp/core/support/ThreadPoolCreator.java`

2. **ThreadPoolBuilder** - 线程池构建器
   - `core/src/main/java/com/dtp/core/thread/ThreadPoolBuilder.java`

3. **DtpExecutor** - 动态线程池执行器
   - `core/src/main/java/com/dtp/core/thread/DtpExecutor.java`

4. **DtpRegistry** - 线程池注册中心（核心：动态调整）
   - `core/src/main/java/com/dtp/core/DtpRegistry.java`
   - 重点关注 `doRefresh()` 方法，这是动态调整的核心实现

5. **AbstractRefresher** - 配置刷新抽象类
   - `core/src/main/java/com/dtp/core/refresh/AbstractRefresher.java`
   - 了解配置中心如何触发刷新

## 模块结构

```
my-debug-module/
├── pom.xml                    # Maven 配置文件
├── README.md                  # 本文件
└── src/main/java/com/dtp/debug/
    ├── DebugMain.java         # 调试入口类
    └── DynamicAdjustDemo.java # 动态调整演示类
```

## 依赖说明

本模块依赖：
- `dynamic-tp-core` - 核心功能模块
- `dynamic-tp-common` - 通用工具模块
- `lombok` - 简化代码（可选）
