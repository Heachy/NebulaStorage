package com.cy.ns.gateway.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.AdminService;
import com.cy.ns.generate.domain.NsAdmin;
import com.cy.ns.generate.entity.param.AdminParam;
import com.cy.ns.generate.service.NsAdminService;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@Service
public class AdminServiceImpl implements AdminService {

    NsAdminService nsAdminService;

    public AdminServiceImpl( NsAdminService nsAdminService ) {
        this.nsAdminService = nsAdminService;
    }


    @Override
    public String login( AdminParam adminParam ) {
        if(adminParam.getAccount() == null || adminParam.getPassword() == null){
            return "账号或密码不能为空";
        }

        QueryWrapper<NsAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "account", adminParam.getAccount() )
                .eq( "is_del",0 );
        NsAdmin nsAdmin = nsAdminService.getOne( queryWrapper );

        if(nsAdmin == null){
            return "账号不存在";
        }

        if(!nsAdmin.getPassword().equals( adminParam.getPassword() )){
            return "密码错误";
        }
        StpUtil.login( nsAdmin.getId() );

        return NsConstants.LOGIN_SUCCESS;
    }

    @Override
    public Boolean logout() {
        try {
            StpUtil.logout();
            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

}
