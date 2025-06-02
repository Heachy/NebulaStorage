package com.cy.ns.raft.enums;

/**
 * @author Haechi
 * @date 2025/3/15 raft角色
 */
public enum RaftRole {
    LEADER(0),
    FOLLOWER(1),
    CANDIDATE(2);

    private final int code;

    RaftRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // 根据代码值获取对应的角色
    public static RaftRole fromCode(int code) {
        for (RaftRole role : RaftRole.values()) {
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
