package com.cy.ns.rs.config;

/**
 * @author Haechi
 */
public class RrsManager {
    public volatile static CodingConfig config;

    public static void setConfig(CodingConfig config) {
        setConfigMethod(config);
    }
    private static void setConfigMethod(CodingConfig config) {
        RrsManager.config = config;
    }
}
