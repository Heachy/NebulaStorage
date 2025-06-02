package com.cy.ns.gateway.controller;

import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.UserService;
import com.cy.ns.generate.entity.param.UserParam;
import com.cy.ns.generate.entity.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    /**
     * 用户注册
     *
     * @param userParam 用户参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public CommonResult<String> userRegister(@RequestBody UserParam userParam ) {
        String register = userService.userRegister( userParam );
        if(register.equals( NsConstants.REGISTER_SUCCESS )){
            return CommonResult.success( register);
        }
        return CommonResult.failed(register);
    }

    @PostMapping("/update/info")
    public CommonResult<String> updateUserInfo(@RequestBody UserParam userParam){
        String msg = userService.updateUserInfo( userParam );
        if(msg.equals( NsConstants.UPDATE_SUCCESS )){
            return CommonResult.success( msg);
        }
        return CommonResult.failed(msg);
    }

    @PostMapping("/update/pwd")
    public CommonResult<String> updateUserPwd(@RequestBody UserParam userParam){
        String msg = userService.updatePassword( userParam );
        if(msg.equals( NsConstants.UPDATE_SUCCESS )){
            return CommonResult.success( msg);
        }
        return CommonResult.failed(msg);
    }

    @PostMapping("/update/phone")
    public CommonResult<String> updateUserPhone(@RequestBody UserParam userParam){
        String msg = userService.updatePhone( userParam );
        if(msg.equals( NsConstants.UPDATE_SUCCESS )){
            return CommonResult.success( msg);
        }
        return CommonResult.failed(msg);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public CommonResult<UserVO> getUserInfo() {
        return CommonResult.success(userService.getUserInfo());
    }




}
