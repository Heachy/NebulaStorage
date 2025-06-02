package com.cy.ns.web.controller;

import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.generate.domain.NSConfig;
import com.cy.ns.generate.entity.param.NsConfigParam;
import com.cy.ns.web.service.ConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@RestController
@RequestMapping("/config")
public class NsConfigController {

    ConfigService configService;

    public NsConfigController( ConfigService configService ) {
        this.configService = configService;
    }

    @GetMapping("/get")
    public CommonResult<NSConfig> getConfig() {
        NSConfig nsConfig = configService.getConfig();
        if( nsConfig != null ) {
            return CommonResult.success( nsConfig );
        } else {
            return CommonResult.failed( "获取配置失败" );
        }
    }

    @PostMapping("/update")
    public CommonResult<String> updateConfig(@RequestBody NsConfigParam nsConfig ) {
        String msg = configService.updateConfig( nsConfig );
        if ( msg != null && msg.equals( NsConstants.UPDATE_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }

}
