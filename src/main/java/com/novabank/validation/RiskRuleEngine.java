package com.novabank.validation;
import java.util.List;
class RiskRuleEngine {
    List<RuleDecision> evaluate(PaymentForValidation payment) { return List.of(new RuleDecision("velocity", true), new RuleDecision("amount", true)); }
}
