package com.cy.ns.gateway.service;

import com.cy.ns.generate.entity.param.UserParam;

/**
 * @author Haechi
 * @date 2025/4/9
 */
public interface LoginService {
    /**
     * 密码登录
     * @param userParam     电话 密码
     * @return token
     */
    String loginByPwd( UserParam userParam);

    /**
     * 通过验证码登录
     * @param userParam 电话 验证码
     * @return token
     */
    String loginByCode( UserParam userParam );

    /**
     * 登出
     *
     * @return 是否成功
     */
    Boolean logout();
}
