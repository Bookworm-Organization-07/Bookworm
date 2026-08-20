import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import { useCart } from '../context/CartContext'
import { effectivePrice, formatRs } from '../utils/price'

export default function CheckoutPage() {
  const { items, loaded, removeFromCart, clearCart, clearCartLocal } = useCart()
  const navigate = useNavigate()

  const [message, setMessage] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)

  if (!loaded) {
    return <div className="page-shell muted py-8">Loading…</div>
  }

  if (result) {
    // Checkout can settle into a purchase transaction, a rental
    // transaction, or both at once (a mixed cart), so this shows
    // whichever actually happened rather than assuming one or the other.
    const buyTransaction = result.find((t) => t.transactionType === 'BUY')
    const rentedSomething = result.some((t) => t.transactionType === 'RENT')

    return (
      <div className="page-shell py-8">
        <h1 className="display-heading mb-4 text-[length:var(--text-2xl)]">Order complete</h1>
        <p className="muted mb-6">Thank you for shopping with Bookworm!</p>
        <div className="flex gap-3">
          {buyTransaction && (
            <Link
              to="/my-shelf"
              className="btn btn-primary"
              state={{ justPurchased: true, transactionId: buyTransaction.transactionId }}
            >
              Go to My Shelf
            </Link>
          )}
          {rentedSomething && (
            <Link to="/my-library" className="btn btn-secondary" state={{ justBorrowed: true }}>
              Go to My Library
            </Link>
          )}
        </div>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="page-shell py-8">
        <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Cart</h1>
        <p className="muted">
          Your cart is empty. <Link to="/genres" className="rail-more">Browse the catalog</Link> to add
          something first.
        </p>
      </div>
    )
  }

  function lineTotal(item) {
    const renting = item.rentDays != null
    const unit = renting
      ? Number(item.product.rentPerDay ?? 0) * Number(item.rentDays)
      : effectivePrice(item.product)
    return unit * item.qty
  }

  const total = items.reduce((sum, item) => sum + lineTotal(item), 0)

  async function handleRemove(cartId) {
    setMessage(null)
    await removeFromCart(cartId)
  }

  async function handlePay(e) {
    e.preventDefault()
    setMessage(null)
    setSubmitting(true)
    try {
      const { data } = await apiClient.post('/checkout')
      clearCartLocal()
      setResult(data.transactions)
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.response?.data?.message ?? 'Something went wrong. Please try again.',
      })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleCancel() {
    setSubmitting(true)
    try {
      await clearCart()
      navigate('/')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page-shell py-8">
      <h1 className="display-heading mb-6 text-[length:var(--text-2xl)]">Cart</h1>

      <div className="grid grid-cols-1 gap-10 md:grid-cols-[1fr_20rem]">
        <div>
          <ul className="flex flex-col gap-4">
            {items.map((item) => {
              const renting = item.rentDays != null
              return (
                <li
                  key={item.cartId}
                  className="flex items-center gap-4 pb-4"
                  style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}
                >
                  <div className="h-20 w-16 shrink-0 overflow-hidden" style={{ background: 'var(--color-paper-2)' }}>
                    {item.product.productImage && (
                      <img
                        src={item.product.productImage}
                        alt={item.product.productName}
                        className="h-full w-full object-cover"
                      />
                    )}
                  </div>
                  <div className="flex-1">
                    <p className="font-medium">{item.product.productName}</p>
                    {item.product.author?.name && (
                      <p className="muted text-xs italic">{item.product.author.name}</p>
                    )}
                    <p className="eyebrow">
                      {renting
                        ? `Rent · ${item.rentDays} day${Number(item.rentDays) === 1 ? '' : 's'} · qty ${item.qty}`
                        : `Purchase · qty ${item.qty}`}
                    </p>
                  </div>
                  <p className="price">{formatRs(lineTotal(item))}</p>
                  <button
                    type="button"
                    onClick={() => handleRemove(item.cartId)}
                    className="btn btn-secondary"
                  >
                    Remove
                  </button>
                </li>
              )
            })}
          </ul>
        </div>

        <div>
          <dl className="flex flex-col gap-2 text-sm">
            <div className="flex justify-between">
              <dt className="muted">Items</dt>
              <dd className="price">{items.length}</dd>
            </div>
            <div
              className="flex justify-between pt-2 text-base font-semibold"
              style={{ borderTop: 'var(--rule-hair) solid var(--color-rule)' }}
            >
              <dt>Total payable</dt>
              <dd className="price">{formatRs(total)}</dd>
            </div>
          </dl>

          <form onSubmit={handlePay} className="mt-6 flex flex-col gap-4">
            {/* Buy vs. Rent was already decided per book when it was added
                to the cart (Add to Cart vs. Rent on the item page), so
                checkout does not offer a way to change it again - it
                just shows which one applies to each line above. */}

            {message && <p className={message.type === 'error' ? 'field-error' : 'muted'}>{message.text}</p>}

            <div className="flex gap-3">
              <button type="submit" disabled={submitting} className="btn btn-primary">
                {submitting ? 'Processing…' : 'Pay'}
              </button>
              <button type="button" onClick={handleCancel} disabled={submitting} className="btn btn-secondary">
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
