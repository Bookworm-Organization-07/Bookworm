import { NavLink, Outlet } from 'react-router-dom'

export default function AdminLayout() {
  return (
    <div className="page-shell py-8">
      <p className="eyebrow mb-2">Bookworm.com · Admin</p>
      <nav
        className="mb-8 flex gap-4 text-sm"
        style={{
          borderBottom: 'var(--rule-hair) solid var(--color-rule)',
          paddingBottom: 'var(--space-sm)',
        }}
      >
        <NavLink to="/admin" end className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Dashboard
        </NavLink>
        <NavLink to="/admin/products" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Products
        </NavLink>
        <NavLink to="/admin/upload" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Readable copies
        </NavLink>
        <NavLink to="/admin/bulk-upload" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Bulk Upload
        </NavLink>
        <NavLink to="/admin/royalties" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Royalties
        </NavLink>
        <NavLink to="/admin/beneficiaries" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Beneficiaries
        </NavLink>
        <NavLink to="/admin/royalty-ledger" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Royalty Ledger
        </NavLink>
        <NavLink to="/admin/users" className={({ isActive }) => (isActive ? 'price' : 'muted')}>
          Users
        </NavLink>
        {/* Orders (BRD's "Purchase History") is marked "Not Mandatory" in
            the BRD - the page, route, and backend endpoint behind it are
            untouched and still work if you navigate to /admin/orders
            directly; it is just not linked from this nav. */}
      </nav>
      <Outlet />
    </div>
  )
}
