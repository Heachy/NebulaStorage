package com.cy.ns.common.api;



/**
 * @author 86189
 */

public enum StatusCode implements IStatusCode {
    //成功
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "无权限！"),
    FAILED(500, "操作失败");
    private final long code;
    private final String message;

    StatusCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}