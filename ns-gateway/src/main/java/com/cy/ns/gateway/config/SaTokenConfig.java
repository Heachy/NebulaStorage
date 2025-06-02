package com.cy.ns.gateway.config;


import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.spring.SaBeanInject;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.google.gson.Gson;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Sa-Token 权限认证] 全局配置类
 *  * @author Haechi
 *  * @date 2025/4/9
 */
@Configuration
@Slf4j
public class SaTokenConfig {

    /**
     * 不校验的路由
     */
    private static final String[] EXCLUDE_PATH = {
            "/login/*",
            "/admin/login",
            "/user/register",
            "/msg/send"};

    /**
     * 公共路由（允许无Token，有Token时校验）
     */
    private static final String[] PUBLIC_PATH = {
            "/ns-web/file/info/**",
            "/ns-web/file/download"};


    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 指定 [拦截路由]
                .addInclude("/**")
                // 指定 [放行路由]
                .addExclude(EXCLUDE_PATH)
                // 指定[认证函数]: 每次请求执行
                .setAuth(obj -> {
                    SaRouter.notMatch( EXCLUDE_PATH);
                    // 处理公共路径：允许无Token，有Token时校验
                    SaRouter.match(PUBLIC_PATH).check( () -> {
                        // 存在Token时进行校验
                        if ( StpUtil.getTokenValue() == null ) {
                            injectGuestToken();
                        }
                    });
                    SaRouter.match("/**", StpUtil::checkLogin );
                } )
                // 指定[异常处理函数]：每次[认证函数]发生异常时执行此函数
                .setError(e -> {
                    CommonResult<Object> result;
                    Gson gson = new Gson();
                    // 处理未登录异常
                    if ( e instanceof NotLoginException notLoginException ) {

                        // 判断场景值，定制化异常信息
                        String message = switch (notLoginException.getType()) {
                            case NotLoginException.NOT_TOKEN -> "未提供token";
                            case NotLoginException.INVALID_TOKEN -> "token无效";
                            case NotLoginException.TOKEN_TIMEOUT -> "token已过期";
                            case NotLoginException.BE_REPLACED -> "token已被顶下线";
                            case NotLoginException.KICK_OUT -> "token已被踢下线";
                            default -> "当前会话未登录";
                        };
                        log.error("未登录异常: {}", message);
                        result = CommonResult.unauthorized(message);
                    }else{
                        // 处理其他异常
                        log.error("其他异常: {}", e.getMessage());
                        result = CommonResult.failed(e.getMessage());
                    }
                    // 其他异常
                    return gson.toJson( result );
                } )
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    SaHolder.getResponse()

                            // ---------- 设置跨域响应头 ----------
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "*")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                    ;

                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match( SaHttpMethod.OPTIONS)
                            .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
                            .back();
                });
    }


    /**
     * 注入访客Token到请求头
     */
    private void injectGuestToken() {
        // 生成唯一访客ID（示例使用UUID）
        String guestId = NsConstants.GUEST_PREFIX + UUID.randomUUID();

        // 为访客创建临时Token（有效期30秒）
        StpUtil.login( guestId, 300 );

        log.debug("注入访客Token: {}", guestId);
    }
}

