package com.cy.ns.gateway.controller;

import com.cy.ns.common.api.CommonResult;
import com.cy.ns.gateway.service.MsgService;
import com.cy.ns.generate.entity.param.UserParam;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 * @author Haechi
 * @date 2025/4/11
 */
@RestController
@RequestMapping("/msg")
public class MsgController {

    @Resource
    MsgService msgService;

    @PostMapping("/send")
    public CommonResult<String> sendMsg(@RequestBody UserParam userParam ) {
        String msg = msgService.sendVerificationMsg(userParam.getPhone());
        if(msg.equals("发送成功")){
            return CommonResult.success(msg);
        }
        return CommonResult.failed(msg);
    }
}
