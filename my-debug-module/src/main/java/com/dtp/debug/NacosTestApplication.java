package com.dtp.debug;

import com.dtp.core.DtpRegistry;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Nacos 动态刷新测试应用
 * 
 * 使用步骤：
 * 1. 启动 Nacos（localhost:8848）
 * 2. 在 Nacos 控制台创建配置（见 Nacos本地测试指南.md）
 * 3. 启动本应用
 * 4. 访问 http://localhost:8080/status 查看线程池状态
 * 5. 在 Nacos 控制台修改配置，观察日志中的动态刷新效果
 * 
 * @author Debug
 */
@Slf4j
@SpringBootApplication
@RestController
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(exclude = {
    com.dtp.starter.nacos.autoconfigure.DtpAutoConfiguration.class
})
public class NacosTestApplication {

    @Resource
    private org.springframework.context.ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(NacosTestApplication.class, args);
        
        // 启动后打印提示信息
        System.out.println("\n========================================");
        System.out.println("Nacos 动态刷新测试应用已启动！");
        System.out.println("========================================");
        System.out.println("1. 访问 http://localhost:8089/status 查看线程池状态");
        System.out.println("2. 在 Nacos 控制台修改配置，观察动态刷新效果");
        System.out.println("3. 修改后再次访问 /status 查看参数变化");
        System.out.println("========================================\n");
    }

    @GetMapping("/status")
    public String getStatus() {
        try {
            String poolName = "test-pool-1";
            DtpExecutor executor = DtpRegistry.getExecutor(poolName);
            
            StringBuilder sb = new StringBuilder();
            sb.append("========================================<br>");
            sb.append("📊 线程池状态: ").append(poolName).append("<br>");
            sb.append("========================================<br>");
            sb.append("核心线程数: ").append(executor.getCorePoolSize()).append("<br>");
            sb.append("最大线程数: ").append(executor.getMaximumPoolSize()).append("<br>");
            sb.append("当前线程数: ").append(executor.getPoolSize()).append("<br>");
            sb.append("活跃线程数: ").append(executor.getActiveCount()).append("<br>");
            sb.append("队列大小: ").append(executor.getQueue().size()).append("<br>");
            sb.append("队列容量: ").append(executor.getQueue().size() + executor.getQueue().remainingCapacity()).append("<br>");
            sb.append("已完成任务数: ").append(executor.getCompletedTaskCount()).append("<br>");
            sb.append("========================================<br>");
            sb.append("💡 提示：在 Nacos 修改配置后刷新此页面查看变化。<br>");
            
            return sb.toString();
        } catch (Exception e) {
            return "❌ 错误: " + e.getMessage() + "<br>请确保 Nacos 中配置的 threadPoolName 为 test-pool-1";
        }
    }

    @GetMapping("/test")
    public String test() {
        try {
            String poolName = "test-pool-1";
            DtpExecutor executor = DtpRegistry.getExecutor(poolName);
            
            // 提交一些任务
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                executor.execute(() -> {
                    log.info("任务 {} 正在执行，线程: {}, 当前线程数: {}, 活跃线程数: {}", 
                            taskId, 
                            Thread.currentThread().getName(),
                            executor.getPoolSize(),
                            executor.getActiveCount());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            return "已提交 5 个任务到 " + poolName + "，查看控制台日志观察线程池行为";
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}
