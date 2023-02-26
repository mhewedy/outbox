package com.github.mhewedy.outbox.cdc;

public class DebeziumUtil {

    public static final String DEBEZIUM_CLASS = "io.debezium.engine.DebeziumEngine";

    public static boolean isDebeziumPresent() {
        try {
            Class.forName(DEBEZIUM_CLASS);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
