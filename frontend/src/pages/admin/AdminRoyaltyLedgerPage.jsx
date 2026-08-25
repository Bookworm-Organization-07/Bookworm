import { useEffect, useState } from 'react'
import apiClient from '../../api/client'
import { formatRs } from '../../utils/price'

const TRANSACTION_TYPE_LABELS = { BUY: 'Buy', RENT: 'Rent', LEND: 'Lent' }

export default function AdminRoyaltyLedgerPage() {
  const [ledger, setLedger] = useState(null)

  useEffect(() => {
    apiClient.get('/royalty-ledger').then((res) => setLedger(res.data))
  }, [])

  if (!ledger) {
    return <p className="muted">Loading…</p>
  }

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Royalty Ledger</h1>
      <p className="muted mb-6 max-w-[65ch]">
        The full history behind the beneficiary totals - one row per royalty calculation, in the
        order it happened, each tied back to the transaction that triggered it.
      </p>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
              <th className="pb-2">Date</th>
              <th className="pb-2">Title</th>
              <th className="pb-2">Transaction</th>
              <th className="pb-2">Amount</th>
              <th className="pb-2">Royalty %</th>
              <th className="pb-2">Royalty</th>
            </tr>
          </thead>
          <tbody>
            {ledger.map((entry) => (
              <tr key={entry.roycalId} style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
                <td className="py-2">{entry.tranDate}</td>
                <td className="py-2">{entry.productName}</td>
                <td className="py-2">
                  #{entry.transactionId} · {TRANSACTION_TYPE_LABELS[entry.transactionType] ?? entry.transactionType}
                </td>
                <td className="price py-2">{formatRs(entry.totalAmount)}</td>
                <td className="py-2">{entry.royaltyPercent}%</td>
                <td className="price py-2">{formatRs(entry.totalRoyalty)}</td>
              </tr>
            ))}
            {ledger.length === 0 && (
              <tr>
                <td colSpan={6} className="muted py-4">
                  No royalty calculations recorded yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
