package com.cy.ns.common.exception;


import com.cy.ns.common.api.IStatusCode;

/**
 * @author Haechi
 */
public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail( IStatusCode statusCode) {
        throw new ApiException(statusCode);
    }
}
