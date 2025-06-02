package com.cy.ns.web.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.cy.ns.common.api.CommonResult;
import com.cy.ns.generate.entity.vo.RaftVO;
import com.cy.ns.web.service.RaftService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@RestController
@RequestMapping("/raft")
@SaCheckRole("admin")
public class RaftController {

    RaftService raftService;


    public RaftController(RaftService raftService) {
        this.raftService = raftService;
    }

    @GetMapping("/info")
    public CommonResult<RaftVO> getRaftInfo() {
        return CommonResult.success(raftService.getRaftInfo());
    }


}
