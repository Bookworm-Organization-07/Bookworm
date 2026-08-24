import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import apiClient from '../api/client'

const AuthContext = createContext(null)

const TOKEN_KEY = 'bookworm_token'
const REFRESH_TOKEN_KEY = 'bookworm_refresh_token'
const USER_KEY = 'bookworm_user'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback(async (email, password) => {
    // /api/auth/login returns { token, refreshToken, userId, userName, admin }.
    const { data } = await apiClient.post('/auth/login', { email, password })
    const { token, refreshToken, ...profile } = data

    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
    setUser(profile)
    return profile
  }, [])

  // Same shape of response as login() above, and stored the exact same
  // way - the rest of the app never needs to know whether a session
  // came from a password or from Google.
  const loginWithGoogle = useCallback(async (idToken) => {
    const { data } = await apiClient.post('/auth/google', { idToken })
    const { token, refreshToken, ...profile } = data

    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
    setUser(profile)
    return profile
  }, [])

  const register = useCallback(async (payload) => {
    const { data } = await apiClient.post('/auth/register', payload)
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  // The access token only lasts 15 minutes. apiClient (see api/client.js)
  // already handles that transparently for as long as the 7-day refresh
  // token is still good - it retries the failed request with a new access
  // token behind the scenes. This only fires once that refresh token has
  // also run out (or been rejected), which apiClient can't recover from by
  // itself: it clears storage and fires this event so the app can drop back
  // to a logged-out state instead of leaving stale "logged in" UI showing.
  useEffect(() => {
    function handleSessionExpired() {
      setUser(null)
    }
    window.addEventListener('bookworm:session-expired', handleSessionExpired)
    return () => window.removeEventListener('bookworm:session-expired', handleSessionExpired)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, loginWithGoogle, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
