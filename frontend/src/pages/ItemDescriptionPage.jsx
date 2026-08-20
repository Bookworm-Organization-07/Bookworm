import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { effectivePrice, isDiscounted, formatRs } from '../utils/price'

export default function ItemDescriptionPage() {
  const { productId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { items, addToCart, rentProduct } = useCart()

  const [product, setProduct] = useState(null)
  const [notFound, setNotFound] = useState(false)
  const [adding, setAdding] = useState(false)
  const [error, setError] = useState(null)

  const [showRentForm, setShowRentForm] = useState(false)
  const [rentDays, setRentDays] = useState(1)
  const [renting, setRenting] = useState(false)

  // Every purchase lands on the reader's shelf (see MyShelfPage), so
  // that is the one place to check for "do they already own this?".
  const [shelfItems, setShelfItems] = useState([])

  // Every current rental or library borrow lands in My Library (see
  // MyLibraryPage) - the one place to check for "is this already
  // rented/lent to them right now?". /my-library only ever returns
  // still-active rows (see MyLibraryRepository.findByUser_UserIdAndEndDateAfter),
  // so an expired rental does not block renting the book again.
  const [libraryItems, setLibraryItems] = useState([])

  useEffect(() => {
    apiClient
      .get(`/products/${productId}`)
      .then((res) => {
        setProduct(res.data)
        setRentDays(Math.max(1, res.data.minRentDays || 1))
      })
      .catch(() => setNotFound(true))
  }, [productId])

  useEffect(() => {
    if (!user) {
      setShelfItems([])
      setLibraryItems([])
      return
    }
    apiClient.get('/shelf').then((res) => setShelfItems(res.data))
    apiClient.get('/my-library').then((res) => setLibraryItems(res.data))
  }, [user])

  if (notFound) {
    return (
      <div className="page-shell py-8">
        <p className="field-error mb-3">Item not found.</p>
        <Link to="/" className="rail-more">
          Back to Home
        </Link>
      </div>
    )
  }

  if (!product) {
    return <div className="page-shell muted py-8">Loading…</div>
  }

  const price = effectivePrice(product)
  const inCart = items.some((item) => item.product?.productId === product.productId)
  const alreadyOwned = shelfItems.some((item) => item.product?.productId === product.productId)
  const alreadyRented = libraryItems.some((item) => item.product?.productId === product.productId)

  function requireLogin() {
    if (!user) {
      navigate('/login')
      return true
    }
    return false
  }

  async function handleAddToCart() {
    if (requireLogin()) return
    setError(null)
    setAdding(true)
    try {
      await addToCart(product.productId)
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not add to cart.')
    } finally {
      setAdding(false)
    }
  }

  async function handleRentSubmit(e) {
    e.preventDefault()
    if (requireLogin()) return
    setError(null)
    setRenting(true)
    try {
      await rentProduct(product.productId, Number(rentDays))
      navigate('/checkout')
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not rent this title.')
    } finally {
      setRenting(false)
    }
  }

  const metaRows = [
    ['Genre', product.genere?.genereDesc],
    ['Language', product.language?.languageDesc],
    ['Author', product.author?.name],
    ['Publisher', product.publisher?.name],
    ['Format', product.productType?.typeDesc],
    ['ISBN', product.productIsbn],
    product.rentable && product.rentPerDay != null
      ? ['Rent', `${formatRs(product.rentPerDay)}/day, minimum ${product.minRentDays} days`]
      : null,
  ].filter((row) => row && row[1])

  return (
    <div className="page-shell py-8">
      <Link to="/genres" className="rail-more">
        &larr; Back
      </Link>

      <div className="mt-6 grid grid-cols-1 gap-8 sm:grid-cols-[16rem_1fr]">
        <div className="book-card__cover" style={{ borderRadius: 'var(--radius-card)' }}>
          {product.productImage ? (
            <img src={product.productImage} alt={product.productName} />
          ) : (
            <span className="eyebrow">No cover</span>
          )}
        </div>

        <div>
          <h1 className="display-heading text-[length:var(--text-2xl)]">{product.productName}</h1>
          {product.productNameEnglish && product.productNameEnglish !== product.productName && (
            <p className="muted italic">{product.productNameEnglish}</p>
          )}
          {product.productDescriptionShort && (
            <p className="muted italic">{product.productDescriptionShort}</p>
          )}

          <p className="price mt-3 text-[length:var(--text-xl)]">
            {formatRs(price)}
            {isDiscounted(product) && (
              <span className="muted ml-2 text-[length:var(--text-base)] line-through">
                {formatRs(product.productBaseprice)}
              </span>
            )}
          </p>

          <dl className="mt-4 divide-y" style={{ borderTop: 'var(--rule-hair) solid var(--color-rule)' }}>
            {metaRows.map(([label, value]) => (
              <div
                key={label}
                className="flex justify-between py-2 text-sm"
                style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}
              >
                <dt className="eyebrow">{label}</dt>
                <dd>{value}</dd>
              </div>
            ))}
          </dl>

          <p className="mt-6 max-w-[65ch] whitespace-pre-line text-[length:var(--text-base)] leading-relaxed">
            {product.productDescriptionLong || 'No synopsis available yet.'}
          </p>

          {error && <p className="field-error mt-4">{error}</p>}

          {alreadyOwned ? (
            // Buying, renting, or borrowing a title you already own would
            // just be paying for it twice, so the buttons are replaced
            // with a small note pointing back at the shelf instead.
            <p className="field-help mt-6" style={{ color: 'var(--color-accent)' }}>
              You already have this book in your shelf.{' '}
              <Link to="/my-shelf" className="rail-more">
                Go to My Shelf
              </Link>
            </p>
          ) : (
            <div className="mt-6 flex flex-wrap gap-3">
              {inCart ? (
                <Link to="/checkout" className="btn btn-primary">
                  In cart — go to checkout
                </Link>
              ) : (
                <button type="button" onClick={handleAddToCart} disabled={adding} className="btn btn-primary">
                  {adding ? 'Adding…' : 'Add to Cart'}
                </button>
              )}

              {/* Renting or borrowing a title that is already rented/lent
                  to this reader would just double up on the same access -
                  buying stays available either way, since owning it
                  outright is always a step up from a temporary rental. */}
              {!alreadyRented && product.rentable && product.rentPerDay != null && !showRentForm && (
                <button type="button" onClick={() => setShowRentForm(true)} className="btn btn-secondary">
                  Rent
                </button>
              )}

              {!alreadyRented && product.library && (
                <Link to="/lending-library" className="btn btn-secondary">
                  Borrow from the library
                </Link>
              )}
            </div>
          )}

          {!alreadyOwned && alreadyRented && (
            <p className="field-help mt-3" style={{ color: 'var(--color-accent)' }}>
              This book is already rented.{' '}
              <Link to="/my-library" className="rail-more">
                Go to My Library
              </Link>
            </p>
          )}

          {!alreadyOwned && !alreadyRented && showRentForm && (
            <form onSubmit={handleRentSubmit} className="mt-4 flex items-end gap-3">
              <div>
                <label className="field-label" htmlFor="rentDays">
                  Days
                </label>
                <input
                  id="rentDays"
                  type="number"
                  min={product.minRentDays || 1}
                  value={rentDays}
                  onChange={(e) => setRentDays(e.target.value)}
                  className="field-input w-24"
                />
              </div>
              <p className="price pb-2">
                Total: {formatRs(Number(product.rentPerDay) * Number(rentDays || 0))}
              </p>
              <button type="submit" disabled={renting} className="btn btn-primary">
                {renting ? 'Adding…' : 'Confirm Rent'}
              </button>
            </form>
          )}

        </div>
      </div>
    </div>
  )
}
