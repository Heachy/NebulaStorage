package com.cy.ns.common.utils;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;


public class MsgUtil {
    public static boolean sendMsgByTxPlatform(String phone,String nums) throws Exception {

        // 短信应用SDK AppID
        // 1400开头
        int appId = 0;

        // 短信应用SDK AppKey
        String appKey = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

        // 短信模板ID
        int templateId = 123456;

        // 签名
        String smsSign = "签名";

        String[] params = {nums};

        SmsSingleSender smsSingleSender = new SmsSingleSender(appId, appKey);

        SmsSingleSenderResult smsSingleSenderResult = smsSingleSender.sendWithParam("86", phone, templateId, params, smsSign, "", "");

        System.out.println(smsSingleSenderResult.errMsg);

        return smsSingleSenderResult.result == 0;
    }

}
