-- =====================================================================
-- Bookworm Delta - Dedicated application DB user
-- =====================================================================
-- Every teammate's MySQL "root" password is different (Windows' MySQL
-- installer makes you set your own during setup), so the backend does
-- NOT log in as root. Instead it logs in as this one dedicated user,
-- with a password fixed here in the repo - see
-- backend/src/main/resources/application.properties.
--
-- Run this file ONCE per machine, as root/an admin account, before
-- 01_schema.sql. It only creates the login and grants it access to the
-- bookworm_delta database - it does not create any tables itself.
-- =====================================================================

CREATE USER IF NOT EXISTS 'bookworm_app'@'localhost' IDENTIFIED BY 'Bookworm@2026';

-- bookworm_delta does not exist yet the first time this runs (that is
-- what 01_schema.sql is for), but GRANT on a not-yet-created database
-- is still valid in MySQL - the privilege just waits for the database
-- to appear.
GRANT ALL PRIVILEGES ON bookworm_delta.* TO 'bookworm_app'@'localhost';

FLUSH PRIVILEGES;
