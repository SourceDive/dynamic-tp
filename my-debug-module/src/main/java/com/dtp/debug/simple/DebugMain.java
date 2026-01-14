package com.dtp.debug.simple;

import com.dtp.core.DtpRegistry;
import com.dtp.core.support.ThreadPoolCreator;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * 调试入口类 - 用于阅读源码
 *
 * @author Debug
 */
@Slf4j
public class DebugMain {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Dynamic-Tp 源码调试入口");
        System.out.println("========================================");

        // 1. 创建一个动态线程池
        System.out.println("\n[步骤1] 创建动态线程池...");
        DtpExecutor executor = ThreadPoolCreator.createDynamicFast("debug-executor");

        // 2. 手动注册到注册中心（通常 Spring 会自动注册，这里手动演示）
        System.out.println("[步骤2] 注册线程池到 DtpRegistry...");
        DtpRegistry.register(executor, "debug-main");

        // 3. 从注册中心获取线程池
        System.out.println("[步骤3] 从 DtpRegistry 获取线程池...");
        DtpExecutor retrievedExecutor = DtpRegistry.getExecutor("debug-executor");

        // 4. 执行一些任务
        System.out.println("[步骤4] 提交任务到线程池...");
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            retrievedExecutor.execute(() -> {
                System.out.println("任务 " + taskId + " 正在执行，线程: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("任务 " + taskId + " 执行完成");
            });
        }

        // 5. 打印线程池信息
        System.out.println("\n[步骤5] 线程池状态信息:");
        System.out.println("线程池名称: " + retrievedExecutor.getThreadPoolName());
        System.out.println("核心线程数: " + retrievedExecutor.getCorePoolSize());
        System.out.println("最大线程数: " + retrievedExecutor.getMaximumPoolSize());
        System.out.println("当前线程数: " + retrievedExecutor.getPoolSize());
        System.out.println("活跃线程数: " + retrievedExecutor.getActiveCount());
        System.out.println("队列大小: " + retrievedExecutor.getQueue().size());
        System.out.println("已完成任务数: " + retrievedExecutor.getCompletedTaskCount());

        // 6. 等待任务完成
        System.out.println("\n[步骤6] 等待任务完成...");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n========================================");
        System.out.println("调试完成！可以在这里打断点阅读源码");
        System.out.println("========================================");
    }
}
