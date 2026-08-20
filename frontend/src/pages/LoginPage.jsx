import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import GoogleSignInButton from '../components/GoogleSignInButton'

export default function LoginPage() {
  const { login, loginWithGoogle } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message ?? 'Login failed. Check your credentials.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleGoogleCredential(idToken) {
    setError(null)
    try {
      await loginWithGoogle(idToken)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message ?? 'Google sign-in failed.')
    }
  }

  return (
    <div className="mx-auto max-w-sm px-4 py-16">
      <h1 className="display-heading mb-6 text-center text-[length:var(--text-2xl)]">Sign on</h1>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="field-label" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="field-input"
          />
        </div>
        <div>
          <label className="field-label" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="field-input"
          />
        </div>

        {error && <p className="field-error">{error}</p>}

        <button type="submit" disabled={submitting} className="btn btn-primary w-full">
          {submitting ? 'Signing on…' : 'Sign on'}
        </button>
      </form>

      <div className="my-6 flex items-center gap-3">
        <hr className="hairline flex-1" />
        <span className="muted text-xs">or</span>
        <hr className="hairline flex-1" />
      </div>

      <div className="flex justify-center">
        <GoogleSignInButton onCredential={handleGoogleCredential} />
      </div>

      <p className="muted mt-4 text-center text-sm">
        Not a member yet?{' '}
        <Link to="/register" className="rail-more">
          Register
        </Link>
      </p>
    </div>
  )
}
