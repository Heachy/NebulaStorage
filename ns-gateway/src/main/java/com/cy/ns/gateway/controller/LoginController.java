package com.cy.ns.gateway.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.LoginService;
import com.cy.ns.generate.entity.param.UserParam;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/6
 */
@RestController
public class LoginController {

    @Resource
    LoginService loginService;

    /**
     * 密码登录
     *
     * @return token
     */
    @PostMapping("/login/pwd")
    public CommonResult<String> loginByPwd(@RequestBody UserParam userParam ) {

        String msg = loginService.loginByPwd( userParam );

        if(msg.equals( NsConstants.LOGIN_SUCCESS )){
            return CommonResult.success( StpUtil.getTokenValue(), msg );
        }

        return CommonResult.failed(msg);
    }

    /**
     * 验证码登录
     * @return token
     */
    @PostMapping("/login/code")
    public CommonResult<String> loginByCode(@RequestBody UserParam userParam ) {

        String msg = loginService.loginByCode( userParam );

        if(msg.equals( NsConstants.LOGIN_SUCCESS )){
            return CommonResult.success( StpUtil.getTokenValue(), msg );
        }

        return CommonResult.failed(msg);
    }

    /**
     * 登出
     *
     * @return 登出结果
     */
    @RequestMapping("/logout")
    public CommonResult<Object> logout() {
        if(loginService.logout()){
            return CommonResult.success("登出成功");
        }
        return CommonResult.failed("登出失败");
    }

    @RequestMapping("/token/test")
    public CommonResult<Object> test() {
        return CommonResult.success(StpUtil.getLoginId());
    }
}
