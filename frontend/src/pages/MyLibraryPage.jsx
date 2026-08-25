import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import apiClient from '../api/client'

export default function MyLibraryPage() {
  const location = useLocation()
  const [entries, setEntries] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    apiClient.get('/my-library').then((res) => setEntries(res.data))
  }, [])

  /**
   * The PDF endpoint needs the bearer token, so it cannot simply be an
   * href. Fetch it as a blob and hand that to the browser instead.
   */
  async function handleRead(productId) {
    setError(null)
    try {
      const res = await apiClient.get(`/my-library/read/${productId}`, {
        responseType: 'blob',
      })
      const url = URL.createObjectURL(res.data)
      window.open(url, '_blank', 'noopener')
    } catch {
      setError('No readable copy has been uploaded for this title yet.')
    }
  }

  if (!entries) {
    return <div className="page-shell muted py-8">Loading…</div>
  }

  return (
    <div className="page-shell py-8">
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">My Library</h1>

      {location.state?.justBorrowed && (
        <p className="field-help mb-6" style={{ color: 'var(--color-accent)' }}>
          Added — the titles have been added below.
        </p>
      )}

      {error && <p className="field-error mb-6">{error}</p>}

      <section>
        <h2 className="eyebrow mb-3">Rented &amp; lent titles</h2>
        {entries.length === 0 ? (
          <p className="muted">
            Nothing rented or lent right now.{' '}
            <Link to="/lending-library" className="rail-more">
              Browse the Lending Library
            </Link>
            .
          </p>
        ) : (
          <div className="rail__grid">
            {entries.map((entry) => (
              <div key={entry.myLibId} className="book-card">
                <Link to={`/products/${entry.product.productId}`} className="book-card__cover">
                  {entry.product.productImage ? (
                    <img src={entry.product.productImage} alt={entry.product.productName} loading="lazy" />
                  ) : (
                    <span className="eyebrow">No cover</span>
                  )}
                </Link>
                <div className="book-card__body">
                  <h3 className="book-card__title">{entry.product.productName}</h3>
                  {entry.product.authorName && (
                    <p className="book-card__subtitle">{entry.product.authorName}</p>
                  )}
                  {/* A rented book has no library package (packageName is
                      empty), so the label reads "Rented" instead. A book
                      borrowed on a package shows that package's name -
                      this is the "clear distinction" the BRD asks for
                      between rented and lent titles. */}
                  <p className="muted text-xs">
                    {entry.accessType === 'RENT' ? 'Rented' : entry.packageName} · until {entry.endDate}
                  </p>
                  <button
                    type="button"
                    onClick={() => handleRead(entry.product.productId)}
                    className="btn btn-secondary mt-2"
                  >
                    Read
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
