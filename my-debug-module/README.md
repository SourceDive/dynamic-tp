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

### 3. 阅读源码建议

建议按照以下顺序阅读源码：

1. **ThreadPoolCreator** - 线程池创建器
   - `core/src/main/java/com/dtp/core/support/ThreadPoolCreator.java`

2. **ThreadPoolBuilder** - 线程池构建器
   - `core/src/main/java/com/dtp/core/thread/ThreadPoolBuilder.java`

3. **DtpExecutor** - 动态线程池执行器
   - `core/src/main/java/com/dtp/core/thread/DtpExecutor.java`

4. **DtpRegistry** - 线程池注册中心
   - `core/src/main/java/com/dtp/core/DtpRegistry.java`

## 模块结构

```
my-debug-module/
├── pom.xml                    # Maven 配置文件
├── README.md                  # 本文件
└── src/main/java/com/dtp/debug/
    └── DebugMain.java         # 调试入口类
```

## 依赖说明

本模块依赖：
- `dynamic-tp-core` - 核心功能模块
- `dynamic-tp-common` - 通用工具模块
- `lombok` - 简化代码（可选）
