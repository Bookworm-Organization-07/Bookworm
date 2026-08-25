import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import apiClient from '../api/client'
import ProductCard from '../components/ProductCard'
import { isDiscounted } from '../utils/price'

export default function HomePage() {
  const [onSale, setOnSale] = useState([])
  const [latestAdditions, setLatestAdditions] = useState([])
  const [featured, setFeatured] = useState([])
  const [error, setError] = useState(null)

  // Product categories for the "Browse by Products" chips - read from
  // product_type_master (same endpoint NavBar's dropdown and GenresPage's
  // filter already use) rather than a fixed list, so a new category
  // added on the database side alone shows up here with no code change.
  const [productTypes, setProductTypes] = useState([])

  useEffect(() => {
    apiClient.get('/product-types').then((res) => setProductTypes(res.data))
  }, [])

  useEffect(() => {
    apiClient
      .get('/products')
      .then((res) => {
        const products = res.data

        // On Sale: anything currently priced below its base price.
        setOnSale(products.filter(isDiscounted).slice(0, 8))

        // Latest Additions: product ids increase as new titles are
        // added, so sorting by id (highest first) gives the newest
        // titles without needing a separate "date added" column.
        const newestFirst = [...products].sort((a, b) => b.productId - a.productId)
        setLatestAdditions(newestFirst.slice(0, 8))

        // Featured: simply the front of the catalogue's default order.
        setFeatured(products.slice(0, 8))
      })
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="page-shell">
      <p className="home-quote">
        “Every book is a journey waiting to begin.”
      </p>

      <p className="muted max-w-[65ch] pb-8 text-lg">
        Marathi, Hindi, Konkani, and English titles — buy, rent, or borrow from the
        lending library.
      </p>

      {error && <p className="field-error mb-8">Could not reach the backend API: {error}</p>}

      <section className="rail">
        <div className="rail__head">
          <h2 className="eyebrow">Browse by Products</h2>
        </div>
        <div className="category-band">
          {productTypes.map((type) => (
            <Link
              key={type.typeId}
              to={`/genres?type=${encodeURIComponent(type.typeDesc)}`}
              className="category-chip"
            >
              {type.typeDesc}
            </Link>
          ))}
        </div>
      </section>

      <section className="rail">
        <div className="rail__head">
          <h2 className="eyebrow">On Sale</h2>
          <Link to="/genres" className="rail-more">
            See more →
          </Link>
        </div>
        <div className="rail__grid">
          {onSale.map((product) => (
            <ProductCard key={product.productId} product={product} />
          ))}
          {onSale.length === 0 && !error && <p className="muted">Nothing on sale right now.</p>}
        </div>
      </section>

      <section className="rail">
        <div className="rail__head">
          <h2 className="eyebrow">Latest Additions</h2>
          <Link to="/genres" className="rail-more">
            See more →
          </Link>
        </div>
        <div className="rail__grid">
          {latestAdditions.map((product) => (
            <ProductCard key={product.productId} product={product} />
          ))}
          {latestAdditions.length === 0 && !error && <p className="muted">No titles yet.</p>}
        </div>
      </section>

      <section className="rail">
        <div className="rail__head">
          <h2 className="eyebrow">Featured</h2>
          <Link to="/genres" className="rail-more">
            See more →
          </Link>
        </div>
        <div className="rail__grid">
          {featured.map((product) => (
            <ProductCard key={product.productId} product={product} />
          ))}
          {featured.length === 0 && !error && <p className="muted">No titles yet.</p>}
        </div>
      </section>
    </div>
  )
}
