package com.cy.ns.gateway.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.LoginService;
import com.cy.ns.gateway.util.RedisUtil;
import com.cy.ns.generate.domain.NSUser;
import com.cy.ns.generate.entity.param.UserParam;
import com.cy.ns.generate.service.NsUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/9
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    NsUserService nsUserService;

    @Resource
    RedisUtil redisUtil;


    @Override
    public String loginByPwd( UserParam userParam ) {

        // queryWrapper
        QueryWrapper<NSUser> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq( "phone", userParam.getPhone() );

        NSUser user = nsUserService.getOne( queryWrapper );

        System.out.println(SaSecureUtil.md5( userParam.getPassword()));

        if ( user == null ) {
            return "用户不存在";
        }
        if ( !user.getPassword().equals( SaSecureUtil.md5( userParam.getPassword()) ) ) {
            return "密码错误";
        }

        StpUtil.login( user.getId() );

        return "登录成功";

    }

    @Override
    public String loginByCode( UserParam userParam ) {

        // queryWrapper
        QueryWrapper<NSUser> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq( "phone", userParam.getPhone() );

        NSUser user = nsUserService.getOne( queryWrapper );

        if ( user == null ) {
            return "用户不存在";
        }
        if ( !userParam.getVerificationCode().equals( redisUtil.get( NsConstants.NS_REDIS_CODE_PREFIX + userParam.getPhone() ) ) ) {
            return "验证码错误";
        }

        StpUtil.login( user.getId() );

        // 删除验证码
        redisUtil.delete( NsConstants.NS_REDIS_CODE_PREFIX + userParam.getPhone() );

        return "登录成功";

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
