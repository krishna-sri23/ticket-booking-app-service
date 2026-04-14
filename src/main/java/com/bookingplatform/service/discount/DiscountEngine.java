package com.bookingplatform.service.discount;

import com.bookingplatform.entity.Offer;
import com.bookingplatform.entity.OfferCity;
import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.entity.OfferTheatre;
import com.bookingplatform.entity.ShowSeat;
import com.bookingplatform.enums.AppliesTo;
import com.bookingplatform.enums.DiscountType;
import com.bookingplatform.enums.RuleType;
import com.bookingplatform.repository.OfferRepository;
import com.bookingplatform.service.discount.rule.RuleEvaluator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central discount engine. Fetches active offers, filters by scope (city/theatre),
 * evaluates rules (via Strategy pattern), and applies discounts in priority order.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountEngine {

    private final OfferRepository offerRepository;
    private final List<RuleEvaluator> ruleEvaluators;

    private Map<RuleType, RuleEvaluator> evaluatorMap;

    @PostConstruct
    public void init() {
        evaluatorMap = new EnumMap<>(RuleType.class);
        for (RuleEvaluator e : ruleEvaluators) {
            evaluatorMap.put(e.supportedType(), e);
        }
        log.info("DiscountEngine initialized with evaluators: {}", evaluatorMap.keySet());
    }

    /**
     * Calculates applicable discounts for a booking.
     * Returns a list of AppliedOffer (offer + discount amount).
     */
    public List<AppliedOffer> calculateDiscounts(DiscountContext context) {
        List<Offer> activeOffers = offerRepository.findActiveOffers(LocalDate.now());
        activeOffers.sort(Comparator.comparingInt(Offer::getPriority));

        List<AppliedOffer> applied = new ArrayList<>();
        boolean nonStackableApplied = false;

        for (Offer offer : activeOffers) {
            // Skip if non-stackable offer already applied
            if (nonStackableApplied) break;

            // Scope check: city/theatre
            if (!scopeMatches(offer, context)) continue;

            // Rule check: ALL rules must pass
            if (!allRulesPass(offer, context)) continue;

            // Compute discount
            BigDecimal discount = computeDiscount(offer, context);
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                applied.add(new AppliedOffer(offer, discount));
                if (Boolean.FALSE.equals(offer.getStackable())) {
                    nonStackableApplied = true;
                }
            }
        }
        return applied;
    }

    private boolean scopeMatches(Offer offer, DiscountContext ctx) {
        // Empty list = global
        if (!offer.getCities().isEmpty()) {
            boolean cityMatch = offer.getCities().stream()
                    .map(OfferCity::getCity)
                    .anyMatch(c -> c.getId().equals(ctx.getCityId()));
            if (!cityMatch) return false;
        }
        if (!offer.getTheatres().isEmpty()) {
            boolean theatreMatch = offer.getTheatres().stream()
                    .map(OfferTheatre::getTheatre)
                    .anyMatch(t -> t.getId().equals(ctx.getTheatre().getId()));
            if (!theatreMatch) return false;
        }
        return true;
    }

    private boolean allRulesPass(Offer offer, DiscountContext ctx) {
        for (OfferRule rule : offer.getRules()) {
            RuleEvaluator evaluator = evaluatorMap.get(rule.getRuleType());
            if (evaluator == null) {
                log.warn("No evaluator for rule type {}, skipping offer {}", rule.getRuleType(), offer.getCode());
                return false;
            }
            if (!evaluator.evaluate(rule, ctx)) return false;
        }
        return true;
    }

    private BigDecimal computeDiscount(Offer offer, DiscountContext ctx) {
        List<ShowSeat> seats = ctx.getSeats();
        BigDecimal total = seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        switch (offer.getAppliesTo()) {
            case PER_BOOKING:
                return applyDiscountValue(offer, total);

            case PER_TICKET:
                return seats.stream()
                        .map(s -> applyDiscountValue(offer, s.getPrice()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

            case NTH_TICKET:
                Integer n = offer.getNthTicketNumber();
                if (n == null || n < 1 || seats.size() < n) return BigDecimal.ZERO;
                // Apply to Nth most expensive ticket (customer-friendly)
                List<ShowSeat> sorted = new ArrayList<>(seats);
                sorted.sort(Comparator.comparing(ShowSeat::getPrice).reversed());
                ShowSeat target = sorted.get(n - 1);
                return applyDiscountValue(offer, target.getPrice());

            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal applyDiscountValue(Offer offer, BigDecimal base) {
        BigDecimal result;
        if (offer.getDiscountType() == DiscountType.PERCENTAGE) {
            result = base.multiply(offer.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            result = offer.getDiscountValue().min(base);
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }
}
