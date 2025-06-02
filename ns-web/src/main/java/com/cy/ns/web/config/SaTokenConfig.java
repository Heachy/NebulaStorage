package com.cy.ns.web.config;

/**
 * @author Haechi
 * @date 2025/4/9
 */

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.cy.ns.common.api.CommonResult;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证 配置类
 */
@Configuration
@Slf4j
public class SaTokenConfig implements WebMvcConfigurer {
    // 注册 Sa-Token 全局过滤器
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico")
                .setAuth(obj -> {
                    // 校验 Same-Token 身份凭证     —— 以下两句代码可简化为：SaSameUtil.checkCurrentRequestToken();
                    String token = SaHolder.getRequest().getHeader( SaSameUtil.SAME_TOKEN);
                    SaSameUtil.checkToken(token);
                })
                .setError(e -> {
                    log.error("请求异常: {}", e.getMessage());
                    return new Gson().toJson( CommonResult.failed(e.getMessage()) );
                } )
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match( SaHttpMethod.OPTIONS)
                            .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
                            .back();
                })
                ;
    }
}

