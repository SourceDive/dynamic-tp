package com.dtp.debug.auto_adjust;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos 配置类 - 手动创建 ConfigService
 * 
 * @author Debug
 */
@Slf4j
@Configuration
public class NacosConfig {

    @Value("${nacos.config.server-addr:127.0.0.1:8091}")
    private String serverAddr;

    @Value("${nacos.config.username:nacos}")
    private String username;

    @Value("${nacos.config.password:nacos}")
    private String password;

    @Bean
    public ConfigService configService() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("username", username);
            properties.put("password", password);
            properties.put("namespace", ""); 
            
            // 8848 端口使用默认的 contextPath (/nacos)，不需要手动设置
            
            log.info("🚀 准备连接 Nacos (Standard Port): {}, username: {}", serverAddr, username);
            ConfigService configService = NacosFactory.createConfigService(properties);
            log.info("✅ Nacos ConfigService created (v1.x mode), serverAddr: {}, username: {}", serverAddr, username);
            
            // 测试连接
            try {
                String testConfig = configService.getConfig("__test_connection__", "DEFAULT_GROUP", 3000);
                log.info("✅ Nacos connection test completed (HTTP mode)");
            } catch (Exception e) {
                log.warn("⚠️ Nacos connection test warning: {}", e.getMessage());
            }
            
            return configService;
        } catch (Exception e) {
            log.error("Failed to create Nacos ConfigService", e);
            throw new RuntimeException("Failed to create Nacos ConfigService", e);
        }
    }
}
