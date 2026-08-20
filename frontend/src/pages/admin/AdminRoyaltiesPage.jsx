import { useEffect, useState } from 'react'
import apiClient from '../../api/client'

/**
 * One product's editable royalty percentage. Keeps its own draft value
 * so typing in the box does not touch any other row, and only calls the
 * backend once "Save" is clicked.
 */
function RoyaltyRateRow({ product }) {
  const [value, setValue] = useState(product.royaltyPercent ?? '')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  async function handleSave() {
    setSaving(true)
    setSaved(false)
    try {
      await apiClient.patch(`/products/${product.productId}/royalty`, { royaltyPercent: value || 0 })
      setSaved(true)
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <input
        type="number"
        min="0"
        max="100"
        step="0.01"
        value={value}
        onChange={(e) => {
          setValue(e.target.value)
          setSaved(false)
        }}
        className="field-input w-24"
      />
      %
      <button type="button" onClick={handleSave} disabled={saving} className="btn btn-secondary text-xs ml-2">
        {saving ? 'Saving…' : saved ? 'Saved ✓' : 'Save'}
      </button>
    </>
  )
}

/**
 * Add/remove which beneficiaries a book's royalty is split across.
 * Collapsed by default and only loads the current list when opened, so
 * looking at the Royalties page does not fetch (and, for a book with
 * nothing explicitly assigned, silently create) an Author-default row
 * for all ~70 books at once - only the ones an admin actually opens.
 */
function BeneficiaryManager({ product, pool, onPoolChanged }) {
  const [open, setOpen] = useState(false)
  const [assigned, setAssigned] = useState(null)
  const [selectedToAdd, setSelectedToAdd] = useState('')
  const [showNewForm, setShowNewForm] = useState(false)
  const [newName, setNewName] = useState('')
  const [newType, setNewType] = useState('Other')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  function load() {
    apiClient.get(`/products/${product.productId}/beneficiaries`).then((res) => setAssigned(res.data))
  }

  function toggleOpen() {
    if (!open && assigned === null) load()
    setOpen((prev) => !prev)
  }

  async function handleAdd() {
    if (!selectedToAdd) return
    setBusy(true)
    setError(null)
    try {
      await apiClient.post(`/products/${product.productId}/beneficiaries/${selectedToAdd}`)
      setSelectedToAdd('')
      load()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not add this beneficiary.')
    } finally {
      setBusy(false)
    }
  }

  async function handleCreateAndAdd(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const { data: created } = await apiClient.post('/beneficiaries', {
        beneficiaryName: newName,
        beneficiaryType: newType,
      })
      await apiClient.post(`/products/${product.productId}/beneficiaries/${created.beneficiaryId}`)
      setNewName('')
      setNewType('Other')
      setShowNewForm(false)
      onPoolChanged() // so this new beneficiary shows up in every other row's "add existing" list too
      load()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not create this beneficiary.')
    } finally {
      setBusy(false)
    }
  }

  async function handleRemove(beneficiaryId) {
    setBusy(true)
    setError(null)
    try {
      await apiClient.delete(`/products/${product.productId}/beneficiaries/${beneficiaryId}`)
      load()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not remove this beneficiary.')
    } finally {
      setBusy(false)
    }
  }

  const assignedIds = new Set((assigned ?? []).map((b) => b.beneficiaryId))
  const availableToAdd = pool.filter((b) => !assignedIds.has(b.beneficiaryId))

  return (
    <div>
      <button type="button" onClick={toggleOpen} className="btn btn-secondary text-xs">
        {open
          ? 'Hide'
          : assigned
            ? `${assigned.length} beneficiar${assigned.length === 1 ? 'y' : 'ies'}`
            : 'Manage'}
      </button>

      {open && (
        <div className="mt-2 flex flex-col gap-2 text-xs" style={{ minWidth: '16rem' }}>
          {assigned === null && <p className="muted">Loading…</p>}

          {assigned?.map((b) => (
            <div key={b.beneficiaryId} className="flex items-center justify-between gap-2">
              <span>
                {b.beneficiaryName} <span className="muted">({b.beneficiaryType})</span>
              </span>
              <button
                type="button"
                onClick={() => handleRemove(b.beneficiaryId)}
                disabled={busy}
                className="field-error"
                title="Remove from this book"
              >
                ✕
              </button>
            </div>
          ))}

          {assigned && availableToAdd.length > 0 && (
            <div className="flex gap-2">
              <select
                value={selectedToAdd}
                onChange={(e) => setSelectedToAdd(e.target.value)}
                className="field-input flex-1"
              >
                <option value="">Add existing…</option>
                {availableToAdd.map((b) => (
                  <option key={b.beneficiaryId} value={b.beneficiaryId}>
                    {b.beneficiaryName} ({b.beneficiaryType})
                  </option>
                ))}
              </select>
              <button type="button" onClick={handleAdd} disabled={!selectedToAdd || busy} className="btn btn-secondary">
                Add
              </button>
            </div>
          )}

          {assigned && !showNewForm && (
            <button type="button" onClick={() => setShowNewForm(true)} className="rail-more text-left">
              + New beneficiary
            </button>
          )}

          {assigned && showNewForm && (
            <form onSubmit={handleCreateAndAdd} className="flex flex-col gap-2">
              <input
                required
                placeholder="Name"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                className="field-input"
              />
              <select value={newType} onChange={(e) => setNewType(e.target.value)} className="field-input">
                <option value="Author">Author</option>
                <option value="Publisher">Publisher</option>
                <option value="Other">Other</option>
              </select>
              <button type="submit" disabled={busy} className="btn btn-primary">
                Create &amp; add
              </button>
            </form>
          )}

          {error && <p className="field-error">{error}</p>}
        </div>
      )}
    </div>
  )
}

export default function AdminRoyaltiesPage() {
  const [products, setProducts] = useState(null)
  const [beneficiaryPool, setBeneficiaryPool] = useState([])

  useEffect(() => {
    apiClient.get('/products').then((res) => setProducts(res.data))
    refreshPool()
  }, [])

  function refreshPool() {
    apiClient.get('/beneficiaries').then((res) => setBeneficiaryPool(res.data))
  }

  if (!products) {
    return <p className="muted">Loading…</p>
  }

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Royalties</h1>

      <h2 className="eyebrow mb-2">Royalty rate and beneficiaries by book</h2>
      <p className="muted mb-6 max-w-[65ch]">
        How much of each sale, rental, or borrow is set aside as royalty, and who it is split
        across. A book with nothing explicitly assigned defaults to its Author - every book always
        has at least one beneficiary. See Beneficiaries and Royalty Ledger for the payout history.
      </p>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
              <th className="pb-2">Title</th>
              <th className="pb-2">Royalty %</th>
              <th className="pb-2">Beneficiaries</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.productId} style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
                <td className="py-2">{product.productName}</td>
                <td className="py-2 align-top">
                  <RoyaltyRateRow product={product} />
                </td>
                <td className="py-2 align-top">
                  <BeneficiaryManager product={product} pool={beneficiaryPool} onPoolChanged={refreshPool} />
                </td>
              </tr>
            ))}
            {products.length === 0 && (
              <tr>
                <td colSpan={3} className="muted py-4">
                  No products yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
