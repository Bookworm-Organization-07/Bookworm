import { Link } from 'react-router-dom'
import { effectivePrice, isDiscounted, formatRs } from '../utils/price'

export default function ProductCard({ product }) {
  const price = effectivePrice(product)

  return (
    <Link to={`/products/${product.productId}`} className="book-card">
      <div className="book-card__cover">
        {product.productImage ? (
          <img src={product.productImage} alt={product.productName} loading="lazy" />
        ) : (
          <span className="eyebrow">No cover</span>
        )}
      </div>
      <div className="book-card__body">
        <h3 className="book-card__title">{product.productName}</h3>
        {product.productNameEnglish && product.productNameEnglish !== product.productName && (
          <p className="book-card__subtitle">{product.productNameEnglish}</p>
        )}
        {product.author?.name && <p className="book-card__subtitle">{product.author.name}</p>}
        <p className="price">{formatRs(price)}</p>
        {isDiscounted(product) && (
          <p className="muted text-xs line-through">{formatRs(product.productBaseprice)}</p>
        )}
      </div>
    </Link>
  )
}
