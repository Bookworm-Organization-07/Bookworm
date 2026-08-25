import { useEffect, useState } from 'react'
import apiClient from '../../api/client'
import { useAuth } from '../../context/AuthContext'

export default function AdminUsersPage() {
  const { user: signedInAdmin } = useAuth()
  const [users, setUsers] = useState(null)
  const [error, setError] = useState(null)
  const [deletingId, setDeletingId] = useState(null)

  function refreshUsers() {
    apiClient.get('/admin/users').then((res) => setUsers(res.data))
  }

  useEffect(() => {
    refreshUsers()
  }, [])

  async function handleDelete(targetUser) {
    const confirmed = window.confirm(
      `Delete ${targetUser.userName}'s account? They will need to register again to sign back in.`
    )
    if (!confirmed) return

    setError(null)
    setDeletingId(targetUser.userId)
    try {
      await apiClient.delete(`/admin/users/${targetUser.userId}`)
      refreshUsers()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not delete this account.')
    } finally {
      setDeletingId(null)
    }
  }

  if (!users) {
    return <p className="muted">Loading…</p>
  }

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Users</h1>
      <p className="muted mb-6 max-w-[65ch]">
        Every registered account. Deleting one removes their cart, shelf, and library, and signs
        them out for good - they would need to register a new account to come back.
      </p>

      {error && <p className="field-error mb-6">{error}</p>}

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
              <th className="pb-2">Name</th>
              <th className="pb-2">Email</th>
              <th className="pb-2">Role</th>
              <th className="pb-2">Joined</th>
              <th className="pb-2"></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => {
              const isSelf = u.userId === signedInAdmin?.userId
              return (
                <tr key={u.userId} style={{ borderBottom: 'var(--rule-hair) solid var(--color-rule)' }}>
                  <td className="py-2">{u.userName}</td>
                  <td className="py-2">{u.userEmail}</td>
                  <td className="py-2">{u.admin ? 'Admin' : 'Reader'}</td>
                  <td className="py-2">{u.joinDate ?? '—'}</td>
                  <td className="py-2">
                    {!isSelf && (
                      <button
                        type="button"
                        onClick={() => handleDelete(u)}
                        disabled={deletingId === u.userId}
                        className="btn btn-secondary text-xs"
                      >
                        {deletingId === u.userId ? 'Deleting…' : 'Delete'}
                      </button>
                    )}
                  </td>
                </tr>
              )
            })}
            {users.length === 0 && (
              <tr>
                <td colSpan={5} className="muted py-4">
                  No users yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
