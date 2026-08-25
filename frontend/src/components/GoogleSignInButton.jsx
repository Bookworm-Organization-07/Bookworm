import { useEffect, useRef, useState } from 'react'
import apiClient from '../api/client'

const GOOGLE_SCRIPT_SRC = 'https://accounts.google.com/gsi/client'

/**
 * Loads Google's "Sign in with Google" JS library exactly once, no
 * matter how many times a GoogleSignInButton mounts (e.g. the reader
 * visits the login page, leaves, and comes back).
 */
function loadGoogleScript() {
  if (window.google?.accounts?.id) {
    return Promise.resolve()
  }

  const existing = document.querySelector(`script[src="${GOOGLE_SCRIPT_SRC}"]`)
  if (existing) {
    return new Promise((resolve, reject) => {
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', reject)
    })
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = GOOGLE_SCRIPT_SRC
    script.async = true
    script.defer = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Could not load the Google Sign-In script.'))
    document.head.appendChild(script)
  })
}

/**
 * Renders Google's own "Sign in with Google" button. All of the actual
 * sign-in work happens entirely in the browser, between this component
 * and Google - the app's backend is only ever shown the finished result
 * (a signed ID token), via onCredential. See AuthService.loginWithGoogle
 * for what happens to that token next.
 */
export default function GoogleSignInButton({ onCredential }) {
  const buttonRef = useRef(null)
  const [error, setError] = useState(null)

  // Read through a ref instead of depending on onCredential directly -
  // a caller that passes an inline arrow function would otherwise hand
  // this effect a new function every render, re-running the whole
  // Google script/button setup below each time for no reason.
  const onCredentialRef = useRef(onCredential)
  onCredentialRef.current = onCredential

  useEffect(() => {
    let cancelled = false

    async function setUp() {
      try {
        const [{ data }] = await Promise.all([
          apiClient.get('/auth/google/client-id'),
          loadGoogleScript(),
        ])
        if (cancelled || !buttonRef.current) return

        window.google.accounts.id.initialize({
          client_id: data.clientId,
          callback: (response) => onCredentialRef.current(response.credential),
        })
        window.google.accounts.id.renderButton(buttonRef.current, {
          theme: 'outline',
          size: 'large',
          width: 320,
        })
      } catch {
        if (!cancelled) setError('Google Sign-In is not available right now.')
      }
    }

    setUp()
    return () => {
      cancelled = true
    }
  }, [])

  if (error) {
    return <p className="muted text-xs">{error}</p>
  }

  return <div ref={buttonRef} />
}
