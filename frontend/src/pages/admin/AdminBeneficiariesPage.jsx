import { useEffect, useState } from 'react'
import apiClient from '../../api/client'
import { formatRs } from '../../utils/price'

export default function AdminBeneficiariesPage() {
  const [rows, setRows] = useState(null)

  useEffect(() => {
    apiClient.get('/product-beneficiaries').then((res) => setRows(res.data))
  }, [])

  if (!rows) {
    return <p className="muted">Loading…</p>
  }

  const total = rows.reduce((sum, r) => sum + Number(r.royaltyReceived ?? 0), 0)

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Beneficiaries</h1>
      <p className="muted mb-6 max-w-[65ch]">
        Every beneficiary, with the products currently assigned to them and what each has paid out
        so far - beneficiaries with no assigned product yet, or no royalty earned yet, still show up.
      </p>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
              <th className="pb-2">Beneficiary</th>
              <th className="pb-2">Title</th>
              <th className="pb-2">Received</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r, index) => (
              <tr key={`${r.beneficiaryName}-${r.productName ?? 'none'}-${index}`} style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
                <td className="py-2">{r.beneficiaryName}</td>
                <td className="py-2">{r.productName ?? '—'}</td>
                <td className="price py-2">{formatRs(r.royaltyReceived)}</td>
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={3} className="muted py-4">
                  No beneficiaries yet.
                </td>
              </tr>
            )}
          </tbody>
          {rows.length > 0 && (
            <tfoot>
              <tr>
                <td className="py-2 font-semibold" colSpan={2}>
                  Total
                </td>
                <td className="price py-2 font-semibold">{formatRs(total)}</td>
              </tr>
            </tfoot>
          )}
        </table>
      </div>
    </div>
  )
}
