package com.cy.ns.raft.enums;

/**
 * @author Haechi
 * @date 2025/4/21
 */
public enum LogType {
    INFO(0),
    WARN(2),
    ERROR(1);


    private final int code;

    LogType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 根据代码值获取对应的角色
    public static LogType fromCode(int code) {
        for (LogType role : LogType.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    @Override
    public String toString() {
        return "LogType: " + name() + ", Code: " + code;
    }
}
