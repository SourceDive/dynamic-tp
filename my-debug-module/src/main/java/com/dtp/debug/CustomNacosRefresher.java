package com.dtp.debug;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.util.NacosUtil;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.core.support.ThreadPoolCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义 NacosRefresher - 不使用 @NacosInjected
 * 
 * @author Debug
 */
@Slf4j
@Component
public class CustomNacosRefresher extends AbstractRefresher implements InitializingBean, Listener {

    private static final ThreadPoolExecutor EXECUTOR = ThreadPoolCreator.createCommonFast("nacos-listener");

    private ConfigFileTypeEnum configFileType;

    @Resource
    private ConfigService configService;

    @Resource
    private DtpProperties dtpProperties;

    @Resource
    private Environment environment;

    @Override
    public void afterPropertiesSet() {
        if (configService == null) {
            log.warn("ConfigService is null, Nacos refresher will not work");
            return;
        }

        DtpProperties.Nacos nacos = dtpProperties.getNacos();
        if (nacos == null) {
            log.warn("DtpProperties.Nacos is null, Nacos refresher will not work");
            return;
        }

        // 根据配置获取文件类型，默认使用 YML（因为 application.yml 中配置的是 yml）
        configFileType = NacosUtil.getConfigType(dtpProperties, ConfigFileTypeEnum.YML);
        log.info("配置文件类型: {}", configFileType);
        String dataId = NacosUtil.deduceDataId(nacos, environment, configFileType);
        String group = NacosUtil.getGroup(nacos, "DEFAULT_GROUP");

        try {
            log.info("🔍 正在探测 Nacos 配置... dataId: {}, group: {}", dataId, group);
            
            // 尝试获取配置
            String currentConfig = configService.getConfig(dataId, group, 5000);
            
            if (currentConfig == null || currentConfig.trim().isEmpty()) {
                log.warn("⚠️ Nacos 中似乎没有找到配置内容，或者配置为空。请确认 Data ID 和 Group 是否准确。");
            } else {
                log.info("✅ 成功拉取配置！配置长度: {}", currentConfig.length());
            }
            
            // 注册监听器
            configService.addListener(dataId, group, this);
            log.info("✅✅✅ 动态刷新监听器注册成功！");
        } catch (NacosException e) {
            log.error("❌❌❌ Nacos 报错了！");
            log.error("错误代码: {}", e.getErrCode());
            log.error("错误消息: {}", e.getErrMsg());
            if (e.getErrMsg() != null && e.getErrMsg().contains("401")) {
                log.error("💡 提示：这通常意味着用户名或密码不对。");
            }
            if (e.getErrMsg() != null && e.getErrMsg().contains("403")) {
                log.error("💡 提示：这通常意味着权限不足，或者命名空间 ID 填错了。");
            }
        } catch (Exception e) {
            log.error("❌❌❌ 发生了非 Nacos 异常: ", e);
        }
    }

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }

    @Override
    public void receiveConfigInfo(String content) {
        log.info("========================================");
        log.info("🔥 收到 Nacos 配置变更通知！");
        log.info("配置内容长度: {}", content != null ? content.length() : 0);
        log.info("配置类型: {}", configFileType);
        log.info("配置内容预览: {}", content != null && content.length() > 200 ? content.substring(0, 200) + "..." : content);
        log.info("开始刷新线程池参数...");
        
        try {
            refresh(content, configFileType);
            log.info("✅ 线程池参数刷新完成！");
            log.info("========================================");
        } catch (Exception e) {
            log.error("❌ 刷新失败: ", e);
            log.info("========================================");
        }
    }
}
