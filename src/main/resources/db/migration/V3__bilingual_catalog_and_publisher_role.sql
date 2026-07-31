-- Adds English-title search support for native-script (Marathi/Hindi/Konkani) titles,
-- a PUBLISHER stakeholder role, additional language rows, and a seed admin account
-- for the Excel bulk-upload endpoint.

ALTER TABLE products
    ADD COLUMN title_english VARCHAR(255) NULL AFTER title;

INSERT INTO languages (language_code, language_name, is_default) VALUES
    ('mr', 'Marathi', 0),
    ('kok', 'Konkani', 0);

INSERT INTO stakeholder_roles (role_name) VALUES
    ('Publisher');

-- Dev-only seed admin for the Excel upload endpoint. Password: Bookworm@Admin2026
-- (bcrypt hash below). Change this before any real/shared deployment.
INSERT INTO admins (full_name, email, password_hash, role, status) VALUES
    ('Bookworm Admin', 'admin@bookworm.com', '$2b$10$qJuYh4INm3S8laLCTp62l.FYt0OJdctcq2OmXGTqL53a.z1sO8Bly', 'SUPER_ADMIN', 'ACTIVE');
