import { useEffect, useState } from 'react'
import apiClient from '../../api/client'
import { effectivePrice, formatRs } from '../../utils/price'

function CoverCell({ product, onUploaded }) {
  const [uploading, setUploading] = useState(false)

  async function handleFileChosen(e) {
    const file = e.target.files[0]
    if (!file) return

    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      await apiClient.post(`/products/${product.productId}/cover`, formData)
      onUploaded()
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  return (
    <div className="flex items-center gap-2">
      {product.productImage ? (
        <img
          src={product.productImage}
          alt=""
          className="h-12 w-9 object-cover"
          style={{ borderRadius: 'var(--radius-card)' }}
        />
      ) : (
        <span className="muted text-xs">No cover</span>
      )}
      <label className="btn btn-secondary text-xs" style={{ cursor: 'pointer' }}>
        {uploading ? 'Uploading…' : product.productImage ? 'Replace' : 'Upload'}
        <input type="file" accept="image/*" onChange={handleFileChosen} disabled={uploading} hidden />
      </label>
    </div>
  )
}


function DeleteCell({ product, onDeleted }) {
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState(null)

  async function handleClick() {
    if (!window.confirm(`Delete "${product.productName}"? This cannot be undone.`)) {
      return
    }
    setDeleting(true)
    setError(null)
    try {
      await apiClient.delete(`/products/${product.productId}`)
      onDeleted()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not delete this book.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <button type="button" onClick={handleClick} disabled={deleting} className="btn btn-secondary text-xs">
        {deleting ? 'Deleting…' : 'Delete'}
      </button>
      {error && <p className="field-error mt-1 max-w-[16rem] text-xs">{error}</p>}
    </div>
  )
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState(null)

  function refreshProducts() {
    apiClient.get('/products').then((res) => setProducts(res.data))
  }

  useEffect(() => {
    refreshProducts()
  }, [])

  if (!products) {
    return <p className="muted">Loading…</p>
  }

  return (
    <div>
      <h1 className="display-heading mb-6 text-[length:var(--text-2xl)]">Products</h1>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
              <th className="pb-2">Title</th>
              <th className="pb-2">Cover</th>
              <th className="pb-2">Genre</th>
              <th className="pb-2">Language</th>
              <th className="pb-2">Price</th>
              <th className="pb-2">Rentable</th>
              <th className="pb-2">Library</th>
              <th className="pb-2">Royalty</th>
              <th className="pb-2">Delete</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.productId} style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
                <td className="py-2">
                  {p.productName}
                  {p.author?.name && <span className="muted italic"> · {p.author.name}</span>}
                </td>
                <td className="py-2">
                  <CoverCell product={p} onUploaded={refreshProducts} />
                </td>
                <td className="py-2">{p.genere?.genereDesc ?? '—'}</td>
                <td className="py-2">{p.language?.languageDesc ?? '—'}</td>
                <td className="price py-2">{formatRs(effectivePrice(p))}</td>
                <td className="py-2">{p.rentable ? 'Yes' : 'No'}</td>
                <td className="py-2">{p.library ? 'Yes' : 'No'}</td>
                <td className="py-2">{p.royaltyPercent != null ? `${p.royaltyPercent}%` : '—'}</td>
                <td className="py-2">
                  <DeleteCell product={p} onDeleted={refreshProducts} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
