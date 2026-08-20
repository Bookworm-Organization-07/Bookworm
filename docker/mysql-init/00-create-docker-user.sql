-- =====================================================================
-- Docker-only variant of database/00_create_user.sql
-- =====================================================================
-- The real 00_create_user.sql grants 'bookworm_app'@'localhost', which
-- only matches a connection whose client and server are the same
-- machine - true for a bare-metal setup, but not in Compose, where
-- bookworm-api/bookworm-notification connect to the mysql container
-- over the Docker network, not "localhost". Same user/password, just
-- granted from '%' (any host) so the other containers can actually log
-- in. Runs automatically as part of MySQL's own container startup, so
-- there's no root password to supply by hand the way the bare-metal
-- version needs.
-- =====================================================================

CREATE USER IF NOT EXISTS 'bookworm_app'@'%' IDENTIFIED BY 'Bookworm@2026';
GRANT ALL PRIVILEGES ON bookworm_delta.* TO 'bookworm_app'@'%';
FLUSH PRIVILEGES;
