package com.bookingplatform.service.discount.rule;

import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.enums.RuleType;
import com.bookingplatform.service.discount.DiscountContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Evaluates MIN_TICKETS rules.
 * rule_value JSON: {"count":4}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinTicketsRuleEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.MIN_TICKETS;
    }

    @Override
    public boolean evaluate(OfferRule rule, DiscountContext context) {
        try {
            JsonNode node = objectMapper.readTree(rule.getRuleValue());
            int required = node.get("count").asInt();
            return context.getSeats().size() >= required;
        } catch (Exception e) {
            log.warn("Failed to evaluate MIN_TICKETS rule {}: {}", rule.getRuleValue(), e.getMessage());
            return false;
        }
    }
}
