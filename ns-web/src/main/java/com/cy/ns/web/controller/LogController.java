package com.cy.ns.web.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.generate.entity.param.LogParam;
import com.cy.ns.raft.entity.po.ClusterLogPO;
import com.cy.ns.web.service.LogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/21
 */
@RestController
@RequestMapping("/log")
@SaCheckRole("admin")
public class LogController {

    LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/list")
    public CommonResult<Page<ClusterLogPO>> getLogList(@RequestBody LogParam logParam) {
        return CommonResult.success(logService.getLogList(logParam));
    }

}
