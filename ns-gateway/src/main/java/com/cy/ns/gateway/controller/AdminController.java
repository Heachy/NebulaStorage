package com.cy.ns.gateway.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.AdminService;
import com.cy.ns.generate.entity.param.AdminParam;
import com.cy.ns.generate.entity.param.UserParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    AdminService adminService;


    public AdminController( AdminService adminService ) {
        this.adminService = adminService;
    }


    /**
     * 密码登录
     *
     * @return token
     */
    @PostMapping("/login")
    public CommonResult<String> login(@RequestBody AdminParam adminParam ) {

        String msg = adminService.login( adminParam );

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
        if(adminService.logout()){
            return CommonResult.success("登出成功");
        }
        return CommonResult.failed("登出失败");
    }
}
