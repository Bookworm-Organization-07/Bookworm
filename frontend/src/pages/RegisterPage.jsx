import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const initialForm = {
  name: '',
  email: '',
  password: '',
  phone: '',
  address: '',
}

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  function handleReset() {
    setForm(initialForm)
    setError(null)
  }

  function handleCancel() {
    navigate('/')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await register(form)
      setSuccess(true)
    } catch (err) {
      setError(err.response?.data?.message ?? 'Registration failed. Please check your details.')
    } finally {
      setSubmitting(false)
    }
  }

  if (success) {
    return (
      <div className="mx-auto max-w-sm px-4 py-16 text-center">
        <h1 className="display-heading mb-2 text-[length:var(--text-xl)]">You&apos;re registered!</h1>
        <p className="muted mb-6 text-sm">Your account has been created. You can sign on now.</p>
        <Link to="/login" className="btn btn-primary">
          Go to Sign on
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-16">
      <h1 className="display-heading mb-6 text-center text-[length:var(--text-2xl)]">Register</h1>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field label="Full Name" name="name" value={form.name} onChange={handleChange} required />
        <Field label="Email" name="email" type="email" value={form.email} onChange={handleChange} required />
        <Field
          label="Password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          required
          minLength={6}
          helpText="At least 6 characters, including one number or special character."
        />
        <Field label="Phone" name="phone" value={form.phone} onChange={handleChange} />
        <Field label="Address" name="address" value={form.address} onChange={handleChange} />

        {error && <p className="field-error">{error}</p>}

        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={submitting} className="btn btn-primary">
            {submitting ? 'Submitting…' : 'Submit'}
          </button>
          <button type="button" onClick={handleReset} className="btn btn-secondary">
            Reset
          </button>
          <button type="button" onClick={handleCancel} className="btn btn-secondary">
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}

function Field({ label, name, type = 'text', value, onChange, required, minLength, helpText }) {
  return (
    <div>
      <label className="field-label" htmlFor={name}>
        {label}
      </label>
      <input
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        required={required}
        minLength={minLength}
        className="field-input"
      />
      {helpText && <p className="field-help">{helpText}</p>}
    </div>
  )
}
