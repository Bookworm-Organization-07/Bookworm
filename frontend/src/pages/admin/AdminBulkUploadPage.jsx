import { useState } from 'react'
import apiClient from '../../api/client'

const EMPTY_QUICK_ADD_FORM = {
  prodName: '',
  nameEnglish: '',
  type: '',
  language: '',
  genre: '',
  author: '',
  publisher: '',
  price: '',
  specialPrice: '',
  shortDescription: '',
  description: '',
  rentable: false,
  library: false,
}

function QuickAddForm() {
  const [form, setForm] = useState(EMPTY_QUICK_ADD_FORM)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    setMessage(null)
    try {
      const { data } = await apiClient.post('/products/quick-add', {
        ...form,
        price: Number(form.price),
        specialPrice: form.specialPrice ? Number(form.specialPrice) : null,
      })
      setMessage(data.message)
      setForm(EMPTY_QUICK_ADD_FORM)
    } catch (err) {
      setError(err.response?.data?.message ?? 'Could not add this book.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mb-8 grid grid-cols-1 gap-3 sm:grid-cols-2">
      <div>
        <label className="field-label" htmlFor="qa-name">Title</label>
        <input id="qa-name" required className="field-input" value={form.prodName}
          onChange={(e) => updateField('prodName', e.target.value)} />
      </div>
      <div>
        <label className="field-label" htmlFor="qa-name-en">English title (optional)</label>
        <input id="qa-name-en" className="field-input" value={form.nameEnglish}
          onChange={(e) => updateField('nameEnglish', e.target.value)} />
      </div>
      <div>
        <label className="field-label" htmlFor="qa-type">Type (e.g. eBook)</label>
        <input id="qa-type" required className="field-input" value={form.type}
          onChange={(e) => updateField('type', e.target.value)} />
      </div>
      <div>
        <label className="field-label" htmlFor="qa-language">Language</label>
        <input id="qa-language" required className="field-input" value={form.language}
          onChange={(e) => updateField('language', e.target.value)} />
      </div>
      <div>
        <label className="field-label" htmlFor="qa-genre">Genre</label>
        <input id="qa-genre" required className="field-input" value={form.genre}
          onChange={(e) => updateField('genre', e.target.value)} />
      </div>
      <div>
        <label className="field-label" htmlFor="qa-author">Author</label>
        <input id="qa-author" className="field-input" value={form.author}
          onChange={(e) => updateField('author', e.target.value)} />
      </div>
      <div>
        <label className="field-label" htmlFor="qa-publisher">Publisher</label>
        <input id="qa-publisher" className="field-input" value={form.publisher}
          onChange={(e) => updateField('publisher', e.target.value)} />
      </div>
      <div className="flex gap-3">
        <div className="flex-1">
          <label className="field-label" htmlFor="qa-price">Price (Rs)</label>
          <input id="qa-price" type="number" step="0.01" min="0" required className="field-input"
            value={form.price} onChange={(e) => updateField('price', e.target.value)} />
        </div>
        <div className="flex-1">
          <label className="field-label" htmlFor="qa-special-price">Offer price (optional)</label>
          <input id="qa-special-price" type="number" step="0.01" min="0" className="field-input"
            value={form.specialPrice} onChange={(e) => updateField('specialPrice', e.target.value)} />
        </div>
      </div>
      <div className="sm:col-span-2">
        <label className="field-label" htmlFor="qa-short-desc">Short description</label>
        <input id="qa-short-desc" className="field-input" value={form.shortDescription}
          onChange={(e) => updateField('shortDescription', e.target.value)} />
      </div>
      <div className="sm:col-span-2">
        <label className="field-label" htmlFor="qa-desc">Full description</label>
        <textarea id="qa-desc" className="field-input" rows={3} value={form.description}
          onChange={(e) => updateField('description', e.target.value)} />
      </div>
      <div className="sm:col-span-2 flex gap-6 text-sm">
        <label className="flex items-center gap-2">
          <input type="checkbox" checked={form.rentable} onChange={(e) => updateField('rentable', e.target.checked)} />
          Rentable
        </label>
        <label className="flex items-center gap-2">
          <input type="checkbox" checked={form.library} onChange={(e) => updateField('library', e.target.checked)} />
          Lendable through the library
        </label>
      </div>
      <div className="sm:col-span-2">
        <button type="submit" disabled={saving} className="btn btn-primary">
          {saving ? 'Adding…' : 'Add book'}
        </button>
      </div>
      {message && <p className="muted sm:col-span-2">{message}</p>}
      {error && <p className="field-error sm:col-span-2">{error}</p>}
    </form>
  )
}

export default function AdminBulkUploadPage() {
  const [file, setFile] = useState(null)
  const [coverFiles, setCoverFiles] = useState([])
  const [uploading, setUploading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!file) return
    setError(null)
    setResult(null)
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      coverFiles.forEach((coverFile) => formData.append('coverFiles', coverFile))
      const { data } = await apiClient.post('/products/bulk-upload', formData)
      setResult(data)
      setFile(null)
      setCoverFiles([])
      e.target.reset()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Upload failed.')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div>
      <h1 className="display-heading mb-2 text-[length:var(--text-2xl)]">Add books</h1>

      <h2 className="eyebrow mb-2">Add a single book</h2>
      <QuickAddForm />

      <hr className="hairline mb-8" />

      <h2 className="eyebrow mb-4">Bulk upload</h2>

      <form onSubmit={handleSubmit} className="mb-8 flex flex-col gap-3">
        <div>
          <label className="field-label" htmlFor="bulk-xlsx">
            Catalogue file (.xlsx)
          </label>
          <input
            id="bulk-xlsx"
            type="file"
            accept=".xlsx"
            onChange={(e) => setFile(e.target.files[0] ?? null)}
            className="field-input"
          />
        </div>
        <div>
          <label className="field-label" htmlFor="bulk-covers">
            Cover images folder (optional)
          </label>
          <p className="muted mb-1 text-xs">
            Each row is matched by its "cover_id" column - a row with cover_id 105 needs a cover
            file named 105.jpg (or .png, etc).
          </p>
          <input
            id="bulk-covers"
            type="file"
            accept="image/*"
            multiple
            webkitdirectory=""
            onChange={(e) => setCoverFiles(Array.from(e.target.files))}
            className="field-input"
          />
          {coverFiles.length > 0 && (
            <p className="muted mt-1 text-xs">{coverFiles.length} image(s) selected.</p>
          )}
        </div>
        <button type="submit" disabled={!file || uploading} className="btn btn-primary self-start">
          {uploading ? 'Uploading…' : 'Upload'}
        </button>
      </form>

      {error && <p className="field-error mb-6">{error}</p>}

      {result && (
        <div>
          <h2 className="eyebrow mb-2">Result</h2>
          <dl className="mb-4 flex gap-6 text-sm">
            <div>
              <dt className="muted">Rows found</dt>
              <dd className="price">{result.totalRows}</dd>
            </div>
            <div>
              <dt className="muted">Created</dt>
              <dd className="price">{result.createdRows}</dd>
            </div>
            <div>
              <dt className="muted">Skipped</dt>
              <dd className="price">{result.skippedRows}</dd>
            </div>
            <div>
              <dt className="muted">Failed</dt>
              <dd className="price">{result.failedRows}</dd>
            </div>
          </dl>
          <pre
            className="max-h-96 overflow-auto p-3 text-xs"
            style={{
              background: 'var(--color-paper-2)',
              borderRadius: 'var(--radius-card)',
              whiteSpace: 'pre-wrap',
            }}
          >
            {result.log}
          </pre>
        </div>
      )}
    </div>
  )
}
