package com.cy.ns.raft.enums;

/**
 * @author Haechi
 * @date 2025/3/26
 */
public enum CommandType {
    UPLOAD(0),
    DELETE(1),
    UPDATE(2);

    private final int code;

    CommandType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

// 根据代码值获取对应的角色
    public static CommandType fromCode(int code) {
        for (CommandType role : CommandType.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    @Override
    public String toString() {
        return "CommandType: " + name() + ", Code: " + code;
    }
}
