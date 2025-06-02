package com.cy.ns.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.generate.domain.NSConfig;
import com.cy.ns.generate.entity.param.NsConfigParam;
import com.cy.ns.generate.service.NsConfigService;
import com.cy.ns.web.service.ConfigService;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    NsConfigService nsConfigService;

    public ConfigServiceImpl( NsConfigService nsConfigService ) {
        this.nsConfigService = nsConfigService;
    }

    @Override
    public NSConfig getConfig() {
        QueryWrapper<NSConfig> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", StpUtil.getLoginIdAsLong() );
        return nsConfigService.getOne(queryWrapper);
    }


    @Override
    public String updateConfig( NsConfigParam param ) {
        UpdateWrapper<NSConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", StpUtil.getLoginIdAsLong() );
        updateWrapper.set("default_bucket_authority", param.getDefaultBucketAuthority() );
        updateWrapper.set("default_file_authority", param.getDefaultFileAuthority() );

        if ( nsConfigService.update(updateWrapper) ) {
            return NsConstants.UPDATE_SUCCESS;
        }
        return "更新失败";
    }

}
