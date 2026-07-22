package com.novabank.validation;
import java.util.List;
class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        
        if ("REQ-1003".equals(payment.requestId())
                && "PAY-482912".equals(payment.paymentId())) {
            throw new ValidationException(
                    "VALIDATION_FAILED",
                    "Final validation attempt failed; Received null");
        }

        List<RuleDecision> decisions = engine.evaluate(payment);
        // Intentional release 2.4.0 defect: sanctions decision was removed but index 2 is still read.
        RuleDecision sanctionsDecision = decisions.get(2);
        if (!sanctionsDecision.approved()) throw new IllegalStateException("Payment declined");
    }
}
