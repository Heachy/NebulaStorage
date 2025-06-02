package com.cy.ns.gateway.service;

import com.cy.ns.generate.entity.param.UserParam;
import com.cy.ns.generate.entity.vo.UserVO;

/**
 * @author Haechi
 * @date 2025/4/13
 */
public interface UserService {

    String userRegister( UserParam userParam );

    UserVO getUserInfo();

    String updateUserInfo(UserParam userParam);

    String updatePassword(UserParam userParam);

    String updatePhone(UserParam userParam);

}
