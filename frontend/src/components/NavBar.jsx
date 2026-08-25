import { useEffect, useRef, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import apiClient from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { isAdminUser } from '../utils/roles'

export default function NavBar() {
  const { user, logout } = useAuth()
  const { itemCount } = useCart()
  const navigate = useNavigate()
  const admin = isAdminUser(user)
  const dropdownRef = useRef(null)

  // Product categories (eBook, Music, Audio-Book, Film...) for the
  // "Products" dropdown menu - see BRD 4.1 Top Menu Bar. Fetched once
  // when the nav bar first renders, since it's on every page.
  const [productTypes, setProductTypes] = useState([])

  useEffect(() => {
    apiClient.get('/product-types').then((res) => setProductTypes(res.data))
  }, [])

  function handleLogout() {
    logout()
    navigate('/')
  }

  // Closes the <details> dropdown after a link inside it is clicked.
  // Without this it would stay open when the new page renders underneath.
  function closeDropdown(e) {
    e.currentTarget.closest('details')?.removeAttribute('open')
  }

  // Native <details> only closes when you click its OWN <summary> again -
  // it has no built-in "the reader clicked somewhere else, close me"
  // behaviour. This adds that ourselves: while the dropdown is open,
  // listen for clicks anywhere in the whole document, and if the click
  // did not land inside the dropdown, close it.
  useEffect(() => {
    function handleOutsideClick(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        dropdownRef.current.removeAttribute('open')
      }
    }
    document.addEventListener('click', handleOutsideClick)
    return () => document.removeEventListener('click', handleOutsideClick)
  }, [])

  return (
    <header className="site-header">
      <div className="nav-mast">
        <Link to="/" className="mast-name">
          Bookworm.com
        </Link>
      </div>
      <div className="page-shell mast-utility">
        <ul className="mast-links">
          <li>
            <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
              Home
            </NavLink>
          </li>
          <li>
            {/* A native <details>/<summary> dropdown: no click-state to
                manage in React, and it opens/closes with a plain click.
                A <Link> is NOT nested inside <summary> on purpose - a
                clickable link inside a clickable disclosure triangle is
                invalid HTML and behaves inconsistently across browsers.
                "See all products" below does that job instead. */}
            <details className="nav-dropdown" ref={dropdownRef}>
              <summary>Products</summary>
              <div className="nav-dropdown-panel">
                <Link to="/genres" onClick={closeDropdown} className="nav-dropdown-see-all">
                  See all products
                </Link>
                {productTypes.map((type) => (
                  <Link
                    key={type.typeId}
                    to={`/genres?type=${encodeURIComponent(type.typeDesc)}`}
                    onClick={closeDropdown}
                  >
                    {type.typeDesc}
                  </Link>
                ))}
                {productTypes.length === 0 && <span className="muted px-4 py-2 text-xs">Loading…</span>}
              </div>
            </details>
          </li>
          <li>
            <NavLink to="/lending-library" className={({ isActive }) => (isActive ? 'active' : '')}>
              Lending Library
            </NavLink>
          </li>
        </ul>

        <div className="mast-account">
          {user ? (
            <>
              {admin ? (
                <Link to="/admin">Admin</Link>
              ) : (
                <>
                  <Link to="/my-shelf">My Shelf</Link>
                  <Link to="/my-library">My Library</Link>
                  <Link to="/checkout">Cart{itemCount > 0 ? ` (${itemCount})` : ''}</Link>
                </>
              )}
              <span>Hi, {user.userName}</span>
              <button type="button" onClick={handleLogout} className="btn btn-secondary">
                Sign off
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Sign on</Link>
              <Link to="/register" className="btn btn-primary">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
      <hr className="mast-rule" aria-hidden="true" />
    </header>
  )
}
