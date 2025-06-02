package com.cy.ns.web.controller;

import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.web.service.UserService;
import jakarta.annotation.Resource;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;


    @RequestMapping("/update/avatar")
    public CommonResult<String> updateAvatar(@RequestParam("file")  MultipartFile file) throws IOException {
        String msg = userService.updateAvatar(file);
        if (msg.equals(NsConstants.UPDATE_SUCCESS)) {
            return CommonResult.success(msg);
        }
        return CommonResult.failed(msg);
    }


}
