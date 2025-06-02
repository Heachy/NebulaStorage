package com.cy.ns.gateway.service.impl;

import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.MsgService;
import com.cy.ns.gateway.util.RedisUtil;
import jakarta.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 * @date 2025/4/11
 */
@Service
public class MsgServiceImpl implements MsgService {

    @Resource
    RedisUtil redisUtil;

    @Override
    public String sendVerificationMsg(String phone) {

        // 先校验验证码是否过期
        Long expire = redisUtil.getExpire(NsConstants.NS_REDIS_CODE_PREFIX + phone );

        if(expire != null && expire > 0){
            return "验证码未过期，请稍后再试";
        }

        // 设置验证码
        redisUtil.setEx( NsConstants.NS_REDIS_CODE_PREFIX + phone, generateCode(), 5, TimeUnit.MINUTES );

        return NsConstants.MSG_SUCCESS;

    }

    private String generateCode() {
        StringBuilder nums = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            Random random = new Random();

            int i1 = random.nextInt(10);

            nums.append(i1);
        }
        return nums.toString();

    }

}
