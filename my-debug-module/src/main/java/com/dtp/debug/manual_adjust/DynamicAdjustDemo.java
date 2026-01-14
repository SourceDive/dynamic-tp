package com.dtp.debug.manual_adjust;

import com.dtp.common.config.ThreadPoolProperties;
import com.dtp.core.DtpRegistry;
import com.dtp.core.support.ThreadPoolCreator;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 动态调整演示 - 展示线程池的动态调整能力
 * 
 * 动态调整体现在：
 * 1. 可以在运行时修改核心线程数（corePoolSize）
 * 2. 可以在运行时修改最大线程数（maximumPoolSize）
 * 3. 可以在运行时修改线程存活时间（keepAliveTime）
 * 4. 可以在运行时修改队列容量（queueCapacity，仅限 VariableLinkedBlockingQueue）
 * 5. 可以在运行时修改拒绝策略（rejectedHandlerType）
 * 
 * 在实际项目中，这些调整通常通过配置中心（Nacos、Apollo、Zookeeper）触发
 * 
 * @author Debug
 */
@Slf4j
public class DynamicAdjustDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("Dynamic-Tp 动态调整演示");
        System.out.println("========================================");
        
        // 1. 创建初始线程池
        System.out.println("\n[步骤1] 创建初始线程池...");
        DtpExecutor executor = ThreadPoolCreator.createDynamicFast("dynamic-demo");
        DtpRegistry.register(executor, "dynamic-demo");
        
        // 打印初始状态
        printThreadPoolStatus(executor, "初始状态");
        
        // 2. 模拟动态调整核心线程数
        System.out.println("\n[步骤2] 动态调整核心线程数: 1 -> 5");
        ThreadPoolProperties properties1 = new ThreadPoolProperties();
        properties1.setThreadPoolName("dynamic-demo");
        properties1.setCorePoolSize(5);  // 从默认值调整为 5
        // 直接调用 doRefresh，这是实际执行调整的方法（refresh 需要 Spring 环境）
        DtpRegistry.doRefresh(executor, properties1);
        printThreadPoolStatus(executor, "调整核心线程数后");
        
        Thread.sleep(1000);
        
        // 3. 模拟动态调整最大线程数
        System.out.println("\n[步骤3] 动态调整最大线程数: " + executor.getMaximumPoolSize() + " -> 20");
        ThreadPoolProperties properties2 = new ThreadPoolProperties();
        properties2.setThreadPoolName("dynamic-demo");
        properties2.setCorePoolSize(5);  // 保持核心线程数
        properties2.setMaximumPoolSize(20);  // 调整最大线程数
        DtpRegistry.doRefresh(executor, properties2);
        printThreadPoolStatus(executor, "调整最大线程数后");
        
        Thread.sleep(1000);
        
        // 4. 模拟动态调整线程存活时间
        System.out.println("\n[步骤4] 动态调整线程存活时间: 30s -> 60s");
        ThreadPoolProperties properties3 = new ThreadPoolProperties();
        properties3.setThreadPoolName("dynamic-demo");
        properties3.setCorePoolSize(5);
        properties3.setMaximumPoolSize(20);
        properties3.setKeepAliveTime(60);  // 调整为 60 秒
        properties3.setUnit(TimeUnit.SECONDS);
        DtpRegistry.doRefresh(executor, properties3);
        printThreadPoolStatus(executor, "调整线程存活时间后");
        
        Thread.sleep(1000);
        
        // 5. 提交一些任务，观察线程池行为
        System.out.println("\n[步骤5] 提交任务，观察线程池行为...");
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("任务 " + taskId + " 执行中，线程: " + Thread.currentThread().getName() + 
                                 ", 当前线程数: " + executor.getPoolSize() + 
                                 ", 活跃线程数: " + executor.getActiveCount());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        Thread.sleep(2000);
        printThreadPoolStatus(executor, "任务执行后");
        
        // 6. 再次动态调整核心线程数（缩小）
        System.out.println("\n[步骤6] 动态缩小核心线程数: 5 -> 2");
        ThreadPoolProperties properties4 = new ThreadPoolProperties();
        properties4.setThreadPoolName("dynamic-demo");
        properties4.setCorePoolSize(2);  // 缩小核心线程数
        properties4.setMaximumPoolSize(20);
        properties4.setKeepAliveTime(60);
        properties4.setUnit(TimeUnit.SECONDS);
        DtpRegistry.doRefresh(executor, properties4);
        printThreadPoolStatus(executor, "缩小核心线程数后");
        
        System.out.println("\n========================================");
        System.out.println("动态调整演示完成！");
        System.out.println("\n关键点：");
        System.out.println("1. 所有调整都是在运行时进行的，无需重启应用");
        System.out.println("2. 调整会立即生效，影响后续的任务执行");
        System.out.println("3. 在实际项目中，这些调整通过配置中心触发");
        System.out.println("4. 配置中心监听配置变化 -> 调用 DtpRegistry.refresh() -> 调用 doRefresh() 更新参数");
        System.out.println("   （注意：在非 Spring 环境下，可以直接调用 doRefresh() 方法）");
        System.out.println("========================================");
    }
    
    private static void printThreadPoolStatus(DtpExecutor executor, String stage) {
        System.out.println("\n--- " + stage + " ---");
        System.out.println("线程池名称: " + executor.getThreadPoolName());
        System.out.println("核心线程数: " + executor.getCorePoolSize());
        System.out.println("最大线程数: " + executor.getMaximumPoolSize());
        System.out.println("当前线程数: " + executor.getPoolSize());
        System.out.println("活跃线程数: " + executor.getActiveCount());
        System.out.println("队列大小: " + executor.getQueue().size());
        System.out.println("队列容量: " + (executor.getQueue().size() + executor.getQueue().remainingCapacity()));
        System.out.println("已完成任务数: " + executor.getCompletedTaskCount());
    }
}
