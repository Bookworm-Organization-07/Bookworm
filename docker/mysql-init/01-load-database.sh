#!/bin/bash
# =====================================================================
# Why this exists instead of just mounting database/*.sql straight into
# /docker-entrypoint-initdb.d/, and why 04_product_covers.sql is missing
# from the list below
# =====================================================================
# database/04_product_covers.sql is corrupted on disk - not something
# Docker or this script did. Checked directly with `dd`+`od` against the
# raw file bytes (independent of any SQL client or charset setting):
# every one of its embedded cover images starts with EFBFBD... (the
# UTF-8 encoding of the U+FFFD replacement character) instead of a real
# image's actual header (JPEG's FFD8FF..., etc.) - the file was already
# broken before this session touched Docker at all, confirmed by loading
# it against a completely fresh bare-metal database with no Docker
# involved and seeing the identical corruption.
#
# 03_seed_bulk_catalog.sql's own 20 baked-in covers are fine on their
# own - the corruption only showed up because 04_product_covers.sql
# does `DELETE FROM product_cover` before re-inserting all 52 (not just
# its +32), which overwrites 03's 20 good rows with its own corrupted
# copies of the same 20. Loading only 01-03 leaves those 20 intact and
# correct; the other 32 books just have no cover until
# 04_product_covers.sql is regenerated from real source images.
#
# --default-character-set=binary is kept for these three anyway - safe
# even without a corruption to work around (already-valid UTF-8 bytes,
# which is all 01-03 contain, pass through a binary connection
# unchanged), and it's one less thing to reconsider if a future
# regenerated 04_product_covers.sql gets added back to this list.
#
# The files are mounted at /sql-source/ (not directly under
# /docker-entrypoint-initdb.d/) so MySQL's own init-script scanner
# doesn't also try to run them itself on top of this script.
# =====================================================================
set -euo pipefail

for f in 01_schema.sql 02_seed.sql 03_seed_bulk_catalog.sql; do
    echo "Loading $f (--default-character-set=binary)..."
    mysql --default-character-set=binary -uroot -p"${MYSQL_ROOT_PASSWORD}" < "/sql-source/$f"
done
