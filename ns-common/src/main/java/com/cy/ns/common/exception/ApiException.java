package com.cy.ns.common.exception;


import com.cy.ns.common.api.IStatusCode;
import lombok.Getter;

public class ApiException extends RuntimeException{
    @Getter
    private IStatusCode statusCode;

    public ApiException(IStatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }
    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
