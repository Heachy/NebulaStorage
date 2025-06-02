package com.cy.ns.web.service;

import com.cy.ns.generate.domain.NSConfig;
import com.cy.ns.generate.entity.param.NsConfigParam;

/**
 * @author Haechi
 * @date 2025/4/20
 */
public interface ConfigService {
    NSConfig getConfig();

    String updateConfig( NsConfigParam param );
}
