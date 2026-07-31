package com.bookworm.repository;

import com.bookworm.entity.Product;
import com.bookworm.entity.ProductStakeholder;
import com.bookworm.entity.ProductTag;
import com.bookworm.entity.Stakeholder;
import com.bookworm.entity.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Product> hasCategory(Integer categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId);
    }

    public static Specification<Product> hasGenre(Integer genreId) {
        return (root, query, cb) -> cb.equal(root.get("genre").get("genreId"), genreId);
    }

    public static Specification<Product> isFeatured() {
        return (root, query, cb) -> cb.isTrue(root.get("isFeatured"));
    }

    public static Specification<Product> isBestseller() {
        return (root, query, cb) -> cb.isTrue(root.get("isBestseller"));
    }

    public static Specification<Product> isLendable() {
        return (root, query, cb) -> cb.isTrue(root.get("isLendable"));
    }

    public static Specification<Product> isRentable() {
        return (root, query, cb) -> cb.isTrue(root.get("isRentable"));
    }

    /** BRD §5.1: search by title, genre, keyword (tags), or partner/stakeholder name. */
    public static Specification<Product> matchesSearchTerm(String term) {
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Product, ProductTag> tagJoin = root.join("productTags", JoinType.LEFT);
            Join<ProductTag, Tag> tag = tagJoin.join("tag", JoinType.LEFT);
            Join<Product, ProductStakeholder> stakeholderJoin = root.join("productStakeholders", JoinType.LEFT);
            Join<ProductStakeholder, Stakeholder> stakeholder = stakeholderJoin.join("stakeholder", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("titleEnglish"), "")), pattern),
                    cb.like(cb.lower(root.get("genre").get("genreName")), pattern),
                    cb.like(cb.lower(tag.get("tagName")), pattern),
                    cb.like(cb.lower(stakeholder.get("fullName")), pattern)
            );
        };
    }
}
