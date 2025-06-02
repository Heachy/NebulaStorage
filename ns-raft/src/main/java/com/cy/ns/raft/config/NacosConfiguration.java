package com.cy.ns.raft.config;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/4/6
 */
@Component
public class NacosConfiguration {

    @Resource
    private NacosAutoServiceRegistration nacosAutoServiceRegistration;

    /**
     * nacos是否注册完成
     */
    private volatile boolean nacosRegisterFinish = false;

    /**
     * 启动nacos注册
     */
    public void start() {
        if (nacosRegisterFinish ) {
            return;
        }
        try {
            Field declaredField = nacosAutoServiceRegistration.getClass().getDeclaredField("registration");
            declaredField.setAccessible(true);
            NacosRegistration nacosRegistration = (NacosRegistration) declaredField.get(nacosAutoServiceRegistration);
            declaredField.setAccessible(false);
            // 如果开启了自动注册 那么就直接返回
            if (nacosRegistration.isRegisterEnabled()) {
                return;
            }
            nacosRegistration.getNacosDiscoveryProperties().setRegisterEnabled(true);
            nacosAutoServiceRegistration.start();
            nacosRegisterFinish = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 停止nacos注册
     */
    public void stop() {
        if (nacosAutoServiceRegistration != null && nacosRegisterFinish) {
            nacosAutoServiceRegistration.stop();
        }
        nacosRegisterFinish = false;
    }

}
