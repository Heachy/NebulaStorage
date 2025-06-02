package com.cy.ns.gateway.service;

import com.cy.ns.generate.entity.param.AdminParam;

/**
 * @author Haechi
 * @date 2025/4/20
 */
public interface AdminService {

    String login( AdminParam adminParam);

    Boolean logout();
}
