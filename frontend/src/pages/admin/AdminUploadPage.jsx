import { useEffect, useState } from 'react'
import apiClient from '../../api/client'

export default function AdminUploadPage() {
  const [products, setProducts] = useState([])
  const [productId, setProductId] = useState('')
  const [file, setFile] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    apiClient.get('/products').then((res) => setProducts(res.data))
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    if (!file || !productId) return
    setMessage(null)
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      const { data } = await apiClient.post(`/books/${productId}/upload`, formData)
      setMessage({ type: 'success', text: data.message })
      setFile(null)
      e.target.reset()
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message ?? 'Upload failed.' })
    } finally {
      setUploading(false)
    }
  }

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Readable copies</h1>
      <p className="muted mb-6 max-w-[65ch]">
        Attach the PDF a reader opens from My Library. One PDF per title — uploading again
        replaces the existing file. Only readers who currently have the title borrowed can
        open it.
      </p>

      <form onSubmit={handleSubmit} className="mb-8 flex flex-wrap items-end gap-3">
        <div>
          <label className="field-label" htmlFor="productId">
            Title
          </label>
          <select
            id="productId"
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
            className="field-input"
            required
          >
            <option value="">Choose a title…</option>
            {products.map((p) => (
              <option key={p.productId} value={p.productId}>
                {p.productName}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="field-label" htmlFor="pdf">
            PDF
          </label>
          <input
            id="pdf"
            type="file"
            accept="application/pdf"
            onChange={(e) => setFile(e.target.files[0] ?? null)}
            className="field-input"
            required
          />
        </div>

        <button type="submit" disabled={!file || !productId || uploading} className="btn btn-primary">
          {uploading ? 'Uploading…' : 'Upload'}
        </button>
      </form>

      {message && (
        <p className={message.type === 'error' ? 'field-error' : 'field-help'}>{message.text}</p>
      )}
    </div>
  )
}
