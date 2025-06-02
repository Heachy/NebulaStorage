package com.cy.ns.rs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 86189
 */
@Component
public class RsBeanInject {
    public RsBeanInject(@Autowired(required = false) CodingConfig codingConfig){
        if(codingConfig != null) {
            RrsManager.setConfig(codingConfig);
        }
    }
}
