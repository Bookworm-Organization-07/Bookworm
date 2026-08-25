import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/AuthContext'
import { formatRs } from '../utils/price'

export default function LendingLibraryPage() {
  const navigate = useNavigate()
  const { user } = useAuth()

  const [products, setProducts] = useState([])
  const [packages, setPackages] = useState([])
  const [selectedPackage, setSelectedPackage] = useState(null)
  const [selectedProducts, setSelectedProducts] = useState([])
  const [borrowing, setBorrowing] = useState(false)
  const [error, setError] = useState(null)

  // Titles already bought sit on the shelf (see MyShelfPage) - no point
  // borrowing one you already own outright, so those ids get flagged
  // below.
  const [ownedProductIds, setOwnedProductIds] = useState([])

  // Titles already rented or lent to this reader right now sit in My
  // Library (see MyLibraryPage). /my-library only returns still-active
  // rows, so a book whose rental has expired is not flagged here and
  // can be lent again.
  const [rentedProductIds, setRentedProductIds] = useState([])

  useEffect(() => {
    apiClient.get('/products/library').then((res) => setProducts(res.data))
    apiClient.get('/library-packages').then((res) => setPackages(res.data))
  }, [])

  useEffect(() => {
    if (!user) {
      setOwnedProductIds([])
      setRentedProductIds([])
      return
    }
    apiClient.get('/shelf').then((res) => {
      setOwnedProductIds(res.data.map((item) => item.product.productId))
    })
    apiClient.get('/my-library').then((res) => {
      setRentedProductIds(res.data.map((item) => item.product.productId))
    })
  }, [user])

  const activePackage = packages.find((p) => p.packageId === selectedPackage)
  const bookLimit = activePackage?.bookLimit ?? 0

  function toggleProduct(productId) {
    setError(null)
    if (ownedProductIds.includes(productId)) {
      setError('You already have this book in your shelf.')
      return
    }
    if (rentedProductIds.includes(productId)) {
      setError('This book is already rented.')
      return
    }
    setSelectedProducts((prev) => {
      if (prev.includes(productId)) return prev.filter((id) => id !== productId)
      if (prev.length >= bookLimit) {
        setError(`The ${activePackage.name} package allows ${bookLimit} titles.`)
        return prev
      }
      return [...prev, productId]
    })
  }

  async function handleBorrow() {
    if (!user) {
      navigate('/login')
      return
    }
    setError(null)
    setBorrowing(true)
    try {
      await apiClient.post('/library/checkout', {
        packageId: selectedPackage,
        productIds: selectedProducts,
      })
      navigate('/my-library', { state: { justBorrowed: true } })
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not borrow these titles.')
    } finally {
      setBorrowing(false)
    }
  }

  return (
    <div className="page-shell py-8">
      <p className="eyebrow mb-2">Bookworm.com</p>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Lending Library</h1>
      <p className="muted mb-8 max-w-[65ch]">
        Choose a library package, then pick the titles you want to borrow for the length of the
        package's validity period.
      </p>

      {error && <p className="field-error mb-6">{error}</p>}

      <section className="mb-10">
        <h2 className="eyebrow mb-3">1 · Choose a package</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {packages.map((pkg) => {
            const active = pkg.packageId === selectedPackage
            return (
              <div
                key={pkg.packageId}
                className="flex flex-col gap-2 p-4"
                style={{
                  border: `var(--rule-hair) solid ${active ? 'var(--color-accent)' : 'var(--color-rule)'}`,
                  borderRadius: 'var(--radius-card)',
                }}
              >
                <p className="font-medium">{pkg.name}</p>
                <p className="muted text-sm">
                  {pkg.bookLimit} books · {pkg.validityDays} days
                </p>
                <p className="muted text-xs">{pkg.description}</p>
                <p className="price">{formatRs(pkg.cost)}</p>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedPackage(pkg.packageId)
                    setSelectedProducts([])
                    setError(null)
                  }}
                  className={`btn mt-2 ${active ? 'btn-primary' : 'btn-secondary'}`}
                >
                  {active ? 'Selected' : 'Choose'}
                </button>
              </div>
            )
          })}
          {packages.length === 0 && <p className="muted">No packages available right now.</p>}
        </div>
      </section>

      <hr className="hairline mb-8" />

      <div className="mb-3 flex items-baseline justify-between">
        <h2 className="eyebrow">
          2 · Pick your titles
          {activePackage && ` (${selectedProducts.length} of ${bookLimit})`}
        </h2>
        {selectedProducts.length > 0 && (
          <button type="button" onClick={handleBorrow} disabled={borrowing} className="btn btn-primary">
            {borrowing ? 'Borrowing…' : `Borrow ${selectedProducts.length} title${selectedProducts.length === 1 ? '' : 's'}`}
          </button>
        )}
      </div>

      {!activePackage && <p className="muted mb-4">Choose a package above to start picking titles.</p>}

      <div className="rail__grid">
        {products.map((product) => {
          const chosen = selectedProducts.includes(product.productId)
          const owned = ownedProductIds.includes(product.productId)
          const rented = rentedProductIds.includes(product.productId)
          const blocked = owned || rented
          return (
            <button
              key={product.productId}
              type="button"
              disabled={!activePackage || blocked}
              onClick={() => toggleProduct(product.productId)}
              className="book-card text-left"
              style={{
                borderColor: chosen ? 'var(--color-accent)' : 'var(--color-rule)',
                cursor: activePackage && !blocked ? 'pointer' : 'not-allowed',
                opacity: activePackage && !blocked ? 1 : 0.55,
              }}
            >
              <div className="book-card__cover">
                {product.productImage ? (
                  <img src={product.productImage} alt={product.productName} loading="lazy" />
                ) : (
                  <span className="eyebrow">No cover</span>
                )}
              </div>
              <div className="book-card__body">
                <h3 className="book-card__title">{product.productName}</h3>
                {product.author?.name && <p className="book-card__subtitle">{product.author.name}</p>}
                <p className="price">
                  {owned
                    ? 'Already in your shelf'
                    : rented
                      ? 'Already rented'
                      : chosen
                        ? 'Selected ✓'
                        : 'Tap to select'}
                </p>
              </div>
            </button>
          )
        })}
        {products.length === 0 && <p className="muted">No borrowable titles yet.</p>}
      </div>
    </div>
  )
}
