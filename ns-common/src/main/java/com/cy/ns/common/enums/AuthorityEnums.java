package com.cy.ns.common.enums;

/**
 * @author Haechi
 * @date 2025/3/15 权限
 */
public enum AuthorityEnums {
    ONLY_READ(0),
    READ_WRITE(1),
    FORBIDDEN(2),
    EXTEND(3),;

    private final int code;

    AuthorityEnums(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 根据代码值获取对应的角色
    public static AuthorityEnums fromCode(int code) {
        for ( AuthorityEnums role : AuthorityEnums.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    @Override
    public String toString() {
        return "Role: " + name() + ", Code: " + code;
    }
}
