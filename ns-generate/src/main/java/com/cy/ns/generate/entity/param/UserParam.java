package com.cy.ns.generate.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserParam {

    private String password;
    private String phone;
    private String name;
    private String verificationCode;
    private String newPassword;
}
