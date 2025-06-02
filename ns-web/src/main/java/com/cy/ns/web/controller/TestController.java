package com.cy.ns.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.web.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/9
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    RedisUtil redisUtil;
    @RequestMapping("/token")
    public CommonResult<Object> hello() {
        return CommonResult.success( StpUtil.getLoginId() );
    }

    @RequestMapping("/test")
    public String test() {
        return redisUtil.get( "test:test2" );
    }
}
