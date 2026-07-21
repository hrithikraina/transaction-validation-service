package com.novabank.validation;
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

    String entries() {
        return entries.toString();
    }
}
