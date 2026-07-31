-- Seed data for lookup/taxonomy tables so the catalog isn't empty on first run.

INSERT INTO product_categories (category_name, display_order) VALUES
    ('eBook', 1),
    ('Audio-Book', 2),
    ('Music', 3),
    ('Video/Films', 4);

INSERT INTO languages (language_code, language_name, is_default) VALUES
    ('en', 'English', 1),
    ('hi', 'Hindi', 0);

INSERT INTO stakeholder_roles (role_name) VALUES
    ('Author'),
    ('Narrator'),
    ('Director'),
    ('Producer'),
    ('Translator'),
    ('Composer');

INSERT INTO genres (category_id, genre_name, display_order)
SELECT pc.category_id, seed.genre_name, seed.display_order FROM (
    SELECT 'eBook' AS category, 'Novel' AS genre_name, 1 AS display_order
    UNION ALL SELECT 'eBook', 'Short Stories', 2
    UNION ALL SELECT 'eBook', 'Poetry', 3
    UNION ALL SELECT 'eBook', 'Sci-fi', 4
    UNION ALL SELECT 'eBook', 'Romance', 5
    UNION ALL SELECT 'eBook', 'Psychology', 6
    UNION ALL SELECT 'eBook', 'Thriller', 7
    UNION ALL SELECT 'Audio-Book', 'Fiction', 1
    UNION ALL SELECT 'Audio-Book', 'Non-fiction', 2
    UNION ALL SELECT 'Audio-Book', 'Self-help', 3
    UNION ALL SELECT 'Music', 'Indian Classical', 1
    UNION ALL SELECT 'Music', 'Western Classical', 2
    UNION ALL SELECT 'Music', 'Pop', 3
    UNION ALL SELECT 'Music', 'Jazz', 4
    UNION ALL SELECT 'Video/Films', 'Documentary', 1
    UNION ALL SELECT 'Video/Films', 'Short Film', 2
    UNION ALL SELECT 'Video/Films', 'Feature Film', 3
) AS seed
JOIN product_categories pc ON pc.category_name = seed.category;
