package com.novabank.validation;

import java.io.PrintWriter;
import java.io.StringWriter;

class ValidationLog {
    private final StringBuilder entries = new StringBuilder();

    void info(String message) {
        entries.append("2026-07-17T10:15:27.151Z INFO transaction-validation-service ")
                .append(message)
                .append('\n');
    }

    void error(String message) {
        entries.append("2026-07-17T10:15:27.151Z ERROR transaction-validation-service ")
                .append(message)
                .append('\n');
    }

    void error(String message, Throwable error) {
        error(message);
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        entries.append(stackTrace);
    }

    String entries() {
        return entries.toString();
    }
}
