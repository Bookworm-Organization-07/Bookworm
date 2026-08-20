import { useEffect, useState } from 'react'
import apiClient from '../../api/client'

const NEW_BENEFICIARY_OPTION = '__new__'

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

/**
 * Adds one book without needing a whole spreadsheet. Posts straight to
 * /products/quick-add, which is backed by the exact same import code the
 * bulk .xlsx path uses for each row (see ProductController.quickAdd) -
 * so typing a genre or language that already exists reuses it, and a
 * new one is created automatically, same as in the spreadsheet.
 */
function QuickAddForm() {
  const [form, setForm] = useState(EMPTY_QUICK_ADD_FORM)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)
  const [beneficiaries, setBeneficiaries] = useState([])
  const [beneficiaryChoice, setBeneficiaryChoice] = useState('')
  const [newBeneficiaryName, setNewBeneficiaryName] = useState('')

  function loadBeneficiaries() {
    apiClient.get('/beneficiaries').then((res) => setBeneficiaries(res.data))
  }

  useEffect(() => {
    loadBeneficiaries()
  }, [])

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  /**
   * The beneficiary picked here is not part of ManualProductRequest -
   * ProductRowImportService.importRow (what /quick-add calls) has never
   * known about beneficiaries, and giving it one now would mean touching
   * the same import code the bulk .xlsx path also relies on. Instead
   * this reuses the assignment endpoint that already exists for exactly
   * this (see ProductController.assignBeneficiary): create the product
   * first, then - once we have its id back - assign the beneficiary
   * (creating a new one first via POST /beneficiaries if that's what was
   * chosen). A book added without picking anything here behaves exactly
   * as before: no assignment yet, falling back to the Author default the
   * first time it is actually sold/rented/lent.
   */
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

      if (beneficiaryChoice === NEW_BENEFICIARY_OPTION && newBeneficiaryName.trim()) {
        const { data: created } = await apiClient.post('/beneficiaries', {
          beneficiaryName: newBeneficiaryName.trim(),
          beneficiaryType: 'Other',
        })
        await apiClient.post(`/products/${data.productId}/beneficiaries/${created.beneficiaryId}`)
        loadBeneficiaries()
      } else if (beneficiaryChoice && beneficiaryChoice !== NEW_BENEFICIARY_OPTION) {
        await apiClient.post(`/products/${data.productId}/beneficiaries/${beneficiaryChoice}`)
      }

      setMessage(data.message)
      setForm(EMPTY_QUICK_ADD_FORM)
      setBeneficiaryChoice('')
      setNewBeneficiaryName('')
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
      <div>
        <label className="field-label" htmlFor="qa-beneficiary">Beneficiary (optional)</label>
        <select id="qa-beneficiary" className="field-input" value={beneficiaryChoice}
          onChange={(e) => setBeneficiaryChoice(e.target.value)}>
          <option value="">— None (defaults to the author) —</option>
          {beneficiaries.map((b) => (
            <option key={b.beneficiaryId} value={b.beneficiaryId}>{b.beneficiaryName}</option>
          ))}
          <option value={NEW_BENEFICIARY_OPTION}>+ Add new beneficiary…</option>
        </select>
      </div>
      {beneficiaryChoice === NEW_BENEFICIARY_OPTION && (
        <div>
          <label className="field-label" htmlFor="qa-beneficiary-new">New beneficiary name</label>
          <input id="qa-beneficiary-new" className="field-input" value={newBeneficiaryName}
            onChange={(e) => setNewBeneficiaryName(e.target.value)} />
        </div>
      )}
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

/**
 * Lets an admin upload the "Prod Master Table" .xlsx file and turn it
 * into real products in one go, instead of typing each book in by hand.
 *
 * All the actual parsing happens on the backend
 * (ExcelProductImportService + ProductRowImportService) - this page
 * just sends the file and shows whatever comes back: how many rows were
 * found, how many became new products, how many were skipped (already
 * existed, or were a library package row instead of a book), how many
 * failed, and a line-by-line log explaining every single one.
 */
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
      // Every cover goes in under the SAME field name - the backend
      // collects repeated "coverFiles" parts into the list of files it
      // expects. Which row each one belongs to is worked out there by
      // matching the spreadsheet's cover_id column against each file's
      // name (see ExcelProductImportService.FindCoverById on the .NET
      // side) - a row with cover_id "105" needs a cover file named
      // "105.jpg" (or .png, etc).
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
