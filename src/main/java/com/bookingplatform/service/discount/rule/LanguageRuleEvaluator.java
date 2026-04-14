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
 * Evaluates LANGUAGE rules.
 * rule_value JSON: {"languages":["HINDI","ENGLISH"]}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageRuleEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.LANGUAGE;
    }

    @Override
    public boolean evaluate(OfferRule rule, DiscountContext context) {
        try {
            JsonNode node = objectMapper.readTree(rule.getRuleValue());
            String lang = context.getShow().getMovie().getLanguage();
            for (JsonNode l : node.get("languages")) {
                if (l.asText().equalsIgnoreCase(lang)) return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to evaluate LANGUAGE rule {}: {}", rule.getRuleValue(), e.getMessage());
            return false;
        }
    }
}
