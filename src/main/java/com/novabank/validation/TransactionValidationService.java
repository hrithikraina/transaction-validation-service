package com.novabank.validation;
import java.util.List;
class TransactionValidationService {
    private final RiskRuleEngine engine; private final ValidationLog log;
    TransactionValidationService(RiskRuleEngine engine, ValidationLog log) { this.engine = engine; this.log = log; }
    void validate(PaymentForValidation payment) {
        log.info("Evaluating risk rules payment_id=" + payment.paymentId());

        List<RuleDecision> decisions = engine.evaluate(payment);

        RuleDecision sanctionsDecision = decisions.get(2);
        if (!sanctionsDecision.approved()) throw new IllegalStateException("Payment declined");
    }
}
