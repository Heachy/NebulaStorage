package com.cy.ns.gateway.service;

/**
 * @author Haechi
 * @date 2025/4/11
 */
public interface MsgService {

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 发送结果
     */
    String sendVerificationMsg(String phone);
}
