import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import apiClient from '../api/client'

export default function MyShelfPage() {
  const location = useLocation()
  const [items, setItems] = useState(null)

  useEffect(() => {
    apiClient.get('/shelf').then((res) => setItems(res.data))
  }, [])

  return (
    <div className="page-shell py-8">
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">My Shelf</h1>

      {location.state?.justPurchased && (
        <p className="field-help mb-6" style={{ color: 'var(--color-accent)' }}>
          Purchase successful — your item has been added below.
          {location.state.transactionId && (
            <>
              {' '}
              <a
                href={`/api/invoice/${location.state.transactionId}`}
                className="rail-more"
              >
                Download the invoice
              </a>
            </>
          )}
        </p>
      )}

      {!items && <p className="muted">Loading…</p>}

      {items && items.length === 0 && (
        <p className="muted max-w-[65ch]">
          You do not have any item. Please browse through our collection and purchase any item that you like.
        </p>
      )}

      {items && items.length > 0 && (
        <div className="rail__grid">
          {items.map((item) => (
            <Link key={item.shelfId} to={`/products/${item.product.productId}`} className="book-card">
              <div className="book-card__cover">
                {item.product.productImage ? (
                  <img src={item.product.productImage} alt={item.product.productName} loading="lazy" />
                ) : (
                  <span className="eyebrow">No cover</span>
                )}
              </div>
              <div className="book-card__body">
                <h3 className="book-card__title">{item.product.productName}</h3>
                {item.product.author?.name && (
                  <p className="book-card__subtitle">{item.product.author.name}</p>
                )}
                {/* My Shelf only ever holds purchases now - rented books
                    live in My Library instead - so this line never
                    needs to vary. */}
                <p className="muted text-xs">Purchased · yours to keep</p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
