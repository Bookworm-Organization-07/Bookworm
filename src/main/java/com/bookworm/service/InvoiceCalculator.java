package com.bookworm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BRD §12 Invoice Page bill breakdown. The BRD text doesn't itself carry these percentages —
 * they come from the approved DB design doc's citations (discount 5%, VAT 10%, S.C. 2.5%),
 * applied in that order: discount off the subtotal, then VAT and service charge on the
 * discounted amount.
 */
public final class InvoiceCalculator {

    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_CHARGE_RATE = new BigDecimal("0.025");

    private InvoiceCalculator() {
    }

    public record BillBreakdown(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal serviceCharge,
            BigDecimal total) {
    }

    public static BillBreakdown compute(BigDecimal subtotal) {
        BigDecimal discount = subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discounted = subtotal.subtract(discount);
        BigDecimal vat = discounted.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceCharge = discounted.multiply(SERVICE_CHARGE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discounted.add(vat).add(serviceCharge);
        return new BillBreakdown(subtotal, discount, vat, serviceCharge, total);
    }
}
