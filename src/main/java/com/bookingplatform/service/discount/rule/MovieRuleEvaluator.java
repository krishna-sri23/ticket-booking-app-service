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
 * Evaluates MOVIE rules.
 * rule_value JSON: {"movie_ids":[10,11]}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieRuleEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.MOVIE;
    }

    @Override
    public boolean evaluate(OfferRule rule, DiscountContext context) {
        try {
            JsonNode node = objectMapper.readTree(rule.getRuleValue());
            Long movieId = context.getShow().getMovie().getId();
            for (JsonNode id : node.get("movie_ids")) {
                if (id.asLong() == movieId) return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to evaluate MOVIE rule {}: {}", rule.getRuleValue(), e.getMessage());
            return false;
        }
    }
}
