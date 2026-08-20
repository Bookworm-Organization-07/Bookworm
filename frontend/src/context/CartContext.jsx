import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import apiClient from '../api/client'
import { useAuth } from './AuthContext'
import { isAdminUser } from '../utils/roles'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { user } = useAuth()
  const [items, setItems] = useState([])
  const [loaded, setLoaded] = useState(false)

  const refreshCart = useCallback(async () => {
    if (!user || isAdminUser(user)) {
      setItems([])
      setLoaded(true)
      return
    }
    const { data } = await apiClient.get('/cart')
    setItems(data)
    setLoaded(true)
  }, [user])

  useEffect(() => {
    refreshCart()
  }, [refreshCart])

  // Each cart line remembers its own buy-or-rent choice server-side
  // (Cart.rentDays), so adding one book as a purchase and another as a
  // rental never touches the other line's choice.
  const addToCart = useCallback(async (productId, qty = 1) => {
    await apiClient.post('/cart/add', { productId, qty, rentDays: null })
    await refreshCart()
  }, [refreshCart])

  const rentProduct = useCallback(async (productId, days) => {
    await apiClient.post('/cart/add', { productId, qty: 1, rentDays: days })
    await refreshCart()
  }, [refreshCart])

  const updateQty = useCallback(async (cartId, qty) => {
    await apiClient.put(`/cart/${cartId}`, { qty })
    await refreshCart()
  }, [refreshCart])

  const removeFromCart = useCallback(async (cartId) => {
    await apiClient.delete(`/cart/remove/${cartId}`)
    await refreshCart()
  }, [refreshCart])

  const clearCart = useCallback(async () => {
    await apiClient.delete('/cart')
    await refreshCart()
  }, [refreshCart])

  const clearCartLocal = useCallback(() => {
    setItems([])
  }, [])

  const itemCount = items.length

  return (
    <CartContext.Provider
      value={{
        items,
        loaded,
        itemCount,
        refreshCart,
        addToCart,
        rentProduct,
        updateQty,
        removeFromCart,
        clearCart,
        clearCartLocal,
      }}
    >
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within a CartProvider')
  return ctx
}
