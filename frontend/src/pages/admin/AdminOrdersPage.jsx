import { useEffect, useState } from 'react'
import apiClient from '../../api/client'
import { formatRs } from '../../utils/price'

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState(null)

  useEffect(() => {
    apiClient.get('/orders/history').then((res) => setOrders(res.data))
  }, [])

  if (!orders) {
    return <p className="muted">Loading…</p>
  }

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Orders</h1>
      <p className="muted mb-6 max-w-[65ch]">Every line item sold or borrowed, newest first.</p>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
              <th className="pb-2">Transaction</th>
              <th className="pb-2">Title</th>
              <th className="pb-2">Amount</th>
              <th className="pb-2">Date</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o, i) => (
              <tr
                key={`${o.transactionId}-${i}`}
                style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}
              >
                <td className="py-2">#{o.transactionId}</td>
                <td className="py-2">{o.productName}</td>
                <td className="price py-2">{formatRs(o.amount)}</td>
                <td className="py-2">{o.orderDate?.slice(0, 19).replace('T', ' ')}</td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr>
                <td colSpan={4} className="muted py-4">
                  No orders yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
