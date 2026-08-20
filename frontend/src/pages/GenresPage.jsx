import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import apiClient from '../api/client'
import ProductCard from '../components/ProductCard'

/**
 * The "Products" page: everything the catalogue has, with three dropdown
 * filters (Genre, Language, and product Type) that work together.
 *
 * Genre and Language are combined on the BACKEND - the
 * /products/filter endpoint does that matching for us. Type is combined
 * on the FRONTEND instead: once the genre/language-filtered list comes
 * back, it is narrowed further to just the rows whose productType
 * matches, right here in JavaScript. This keeps the backend exactly as
 * simple as it already was (still only ever asked about genre and
 * language) while still letting all three filters work together.
 *
 * All three filters live in the URL as query params
 * (?genre=...&language=...&type=...) so a bookmarked or shared link
 * keeps whatever was selected.
 */
export default function GenresPage() {
  const [searchParams, setSearchParams] = useSearchParams()

  // Reading straight from the URL means the dropdowns and the URL can
  // never disagree with each other.
  const genre = searchParams.get('genre') ?? ''
  const language = searchParams.get('language') ?? ''
  const type = searchParams.get('type') ?? ''
  const search = searchParams.get('search') ?? ''

  const [genres, setGenres] = useState([])
  const [languages, setLanguages] = useState([])
  const [productTypes, setProductTypes] = useState([])
  const [products, setProducts] = useState([])
  const [searchInput, setSearchInput] = useState(search)

  // The three dropdown option lists only need to be fetched once.
  useEffect(() => {
    apiClient.get('/generes').then((res) => setGenres(res.data))
    apiClient.get('/languages').then((res) => setLanguages(res.data))
    apiClient.get('/product-types').then((res) => setProductTypes(res.data))
  }, [])

  // Re-fetch the product list whenever a filter or the search box changes.
  useEffect(() => {
    // Typing a search looks across the whole catalogue by title and
    // ignores every dropdown, same as before.
    if (search) {
      apiClient
        .get('/products/search', { params: { name: search, limit: 50 } })
        .then((res) => setProducts(res.data))
      return
    }

    apiClient
      .get('/products/filter', {
        params: {
          // NOTE: the backend's query parameter is spelled "genere" (a
          // typo baked into the database years ago - see
          // BACKEND_FIXES.md). Everywhere in THIS file we use the
          // correct spelling "genre"; this is the one line where it has
          // to match the backend's existing spelling instead.
          genere: genre || undefined,
          language: language || undefined,
        },
      })
      .then((res) => {
        // Type is narrowed down here, on the frontend, once the
        // genre/language-filtered rows are back - see the note above.
        const rows = type
          ? res.data.filter((p) => p.productType?.typeDesc === type)
          : res.data
        setProducts(rows)
      })
  }, [genre, language, type, search])

  /**
   * Changes one filter (genre, language, or type) without disturbing
   * the other two, and without breaking the "All" option.
   *
   * IMPORTANT: when the reader picks "All" again, we DELETE the query
   * parameter instead of setting it to an empty string. If we left an
   * empty string behind, the URL could end up holding a leftover value
   * from an earlier choice next to the new one, and the backend would
   * then be asked to filter by two things that do not match anything at
   * once - which is exactly the "picking a genre, then switching back to
   * all products, shows nothing" bug. Always rebuild the params fresh
   * and either set or delete each key - never edit the query string by
   * hand.
   *
   * A text search and the dropdowns are two different ways of browsing
   * the same catalogue, and (see the effect above) a search always wins
   * over the dropdowns whenever both are present in the URL at once.
   * So picking a dropdown has to also clear any leftover search -
   * otherwise it would silently keep showing the old search results
   * with the new dropdown having no visible effect at all.
   */
  function updateFilter(key, value) {
    const next = new URLSearchParams(searchParams)
    if (value) {
      next.set(key, value)
    } else {
      next.delete(key)
    }
    next.delete('search')
    setSearchParams(next)
    setSearchInput('')
  }

  /** The reverse of the note above: starting a real search clears the dropdowns, so the URL never holds both at once. */
  function handleSearchSubmit(e) {
    e.preventDefault()
    const next = new URLSearchParams(searchParams)
    if (searchInput) {
      next.set('search', searchInput)
      next.delete('genre')
      next.delete('language')
      next.delete('type')
    } else {
      next.delete('search')
    }
    setSearchParams(next)
  }

  return (
    <div className="page-shell py-8">
      <p className="eyebrow mb-2">Bookworm.com</p>
      <h1 className="display-heading mb-6 text-[length:var(--text-2xl)]">Products</h1>

      <form onSubmit={handleSearchSubmit} className="mb-6 flex gap-2">
        <input
          type="search"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          placeholder="Search the catalogue by title…"
          className="field-input max-w-md"
        />
        <button type="submit" className="btn btn-primary">
          Search
        </button>
      </form>

      <div className="mb-8 flex flex-wrap gap-4">
        <div>
          <label className="field-label" htmlFor="genre-filter">
            Genre
          </label>
          <select
            id="genre-filter"
            className="field-input"
            value={genre}
            onChange={(e) => updateFilter('genre', e.target.value)}
          >
            <option value="">All Genres</option>
            {genres.map((g) => (
              <option key={g.genereId} value={g.genereDesc}>
                {g.genereDesc}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="field-label" htmlFor="language-filter">
            Language
          </label>
          <select
            id="language-filter"
            className="field-input"
            value={language}
            onChange={(e) => updateFilter('language', e.target.value)}
          >
            <option value="">All Languages</option>
            {languages.map((l) => (
              <option key={l.languageId} value={l.languageDesc}>
                {l.languageDesc}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="field-label" htmlFor="type-filter">
            Type
          </label>
          <select
            id="type-filter"
            className="field-input"
            value={type}
            onChange={(e) => updateFilter('type', e.target.value)}
          >
            <option value="">All Types</option>
            {productTypes.map((t) => (
              <option key={t.typeId} value={t.typeDesc}>
                {t.typeDesc}
              </option>
            ))}
          </select>
        </div>
      </div>

      <hr className="hairline mb-8" />

      <div className="rail__grid">
        {products.map((product) => (
          <ProductCard key={product.productId} product={product} />
        ))}
        {products.length === 0 && <p className="muted">No products found.</p>}
      </div>
    </div>
  )
}
