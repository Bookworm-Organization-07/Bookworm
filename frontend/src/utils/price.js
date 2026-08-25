/**
 * Mirrors CheckoutService.unitPrice on the backend so the price shown on
 * a tile matches the price actually charged: a live offer wins, then a
 * discount percentage off the base price, then the base price.
 *
 * The backend remains the authority - this is display only.
 */
export function effectivePrice(product) {
  if (!product) return 0

  const base = Number(product.productBaseprice ?? 0)

  const offerIsLive =
    product.productOfferprice != null &&
    product.productOffPriceExpirydate != null &&
    new Date(product.productOffPriceExpirydate) > new Date()

  if (offerIsLive) return Number(product.productOfferprice)

  const discount = Number(product.discountPercent ?? 0)
  if (discount > 0) return base - (base * discount) / 100

  return base
}

/** True when the title is being sold below its base price right now. */
export function isDiscounted(product) {
  return effectivePrice(product) < Number(product.productBaseprice ?? 0)
}

export function formatRs(value) {
  return `Rs ${Number(value ?? 0).toFixed(2)}`
}
