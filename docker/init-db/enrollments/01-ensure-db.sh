#!/bin/bash
# Create enrollments_db if it does not exist (idempotent; POSTGRES_DB already creates it on first run)
set -e
createdb -U "$POSTGRES_USER" enrollments_db 2>/dev/null || true
