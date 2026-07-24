package com.novabank.validation;

import java.util.List;
import java.util.Optional;

class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }

    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId() + " request_id=" + payment.requestId());

        List<RuleDecision> decisions;
        try {
            decisions = engine.evaluate(payment);
        } catch (Exception e) {
            log.error("Error evaluating risk rules payment_id=" + payment.paymentId() + " request_id=" + payment.requestId(), e);
            throw new ValidationException("Risk rule evaluation failed", e);
        }

        if (decisions == null) {
            log.error("Risk rule engine returned null decisions list payment_id=" + payment.paymentId() + " request_id=" + payment.requestId());
            throw new ValidationException("Missing risk decisions");
        }

        log.info("Risk rule decisions size=" + decisions.size() + " payment_id=" + payment.paymentId() + " request_id=" + payment.requestId());

        Optional<RuleDecision> sanctionsOpt = decisions.stream()
                .filter(d -> "sanctions".equalsIgnoreCase(d.ruleName()))
                .findFirst();

        if (sanctionsOpt.isEmpty()) {
            log.error("Sanctions decision not found in decisions list payment_id=" + payment.paymentId() + " request_id=" + payment.requestId());
            // Controlled validation failure when sanctions decision is absent
            throw new ValidationException("Sanctions decision missing");
        }

        RuleDecision sanctionsDecision = sanctionsOpt.get();
        if (!sanctionsDecision.approved()) {
            log.info("Payment declined by sanctions rule payment_id=" + payment.paymentId() + " request_id=" + payment.requestId());
            throw new IllegalStateException("Payment declined by sanctions rule");
        }
    }

    static class ValidationException extends RuntimeException {
        ValidationException(String message) { super(message); }
        ValidationException(String message, Throwable cause) { super(message, cause); }
    }
}
