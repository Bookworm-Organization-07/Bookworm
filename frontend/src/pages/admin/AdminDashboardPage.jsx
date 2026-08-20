import { useEffect, useState } from 'react'
import apiClient from '../../api/client'
import ProductCard from '../../components/ProductCard'
import { formatRs } from '../../utils/price'

const RECENT_COUNT = 4

/** A single number with a label above it - the building block of the dashboard. */
function StatTile({ label, value }) {
  return (
    <div className="p-4" style={{ border: 'var(--rule-hair) solid var(--color-rule)', borderRadius: 'var(--radius-card)' }}>
      <p className="eyebrow mb-1">{label}</p>
      <p className="price text-[length:var(--text-xl)]">{value}</p>
    </div>
  )
}

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null)
  const [recentProducts, setRecentProducts] = useState([])

  useEffect(() => {
    apiClient.get('/admin/dashboard').then((res) => setStats(res.data))

    // "Recently added" reuses the same idea as the homepage's "Latest
    // Additions" rail: product ids increase as new titles are added, so
    // sorting by id (highest first) gives the newest titles without
    // needing a separate "date added" column.
    apiClient.get('/products').then((res) => {
      const newestFirst = [...res.data].sort((a, b) => b.productId - a.productId)
      setRecentProducts(newestFirst.slice(0, RECENT_COUNT))
    })
  }, [])

  if (!stats) {
    return <p className="muted">Loading…</p>
  }

  return (
    <div>
      <h1 className="display-heading mb-6 text-[length:var(--text-2xl)]">Dashboard</h1>

      <div className="mb-10 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatTile label="Revenue generated" value={formatRs(stats.totalRevenue)} />
        <StatTile label="Books bought, rented & lent" value={stats.booksBought} />
        <StatTile
          label="Bestseller"
          value={stats.bestsellerProductName ? `${stats.bestsellerProductName} (${stats.bestsellerCount})` : '—'}
        />
      </div>

      <h2 className="eyebrow mb-3">Recently added</h2>
      <div className="rail__grid">
        {recentProducts.map((product) => (
          <ProductCard key={product.productId} product={product} />
        ))}
        {recentProducts.length === 0 && <p className="muted">No products yet.</p>}
      </div>
    </div>
  )
}
