#!/bin/bash

set -e
set -u

function create_user_and_database() {
	local database=$1
	local user=${2:-postgres}
	local password=${3:-postgres}

	echo "  Creating user '$user' and database '$database'..."
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    DO \$\$
	    BEGIN
	        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$user') THEN
	            CREATE USER $user WITH ENCRYPTED PASSWORD '$password';
	        END IF;
	    END
	    \$\$;
	    SELECT 'CREATE DATABASE $database'
	    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\\gexec
	    GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
EOSQL
}

echo "Starting DB initialization..."

# Microservices Databases (default postgres user)
create_user_and_database "profile_db"
create_user_and_database "content_db"
create_user_and_database "planning_db"
create_user_and_database "assessment_db"
create_user_and_database "tracking_db"

# Keycloak Database (custom user)
create_user_and_database "keycloak" "keycloak" "password"

echo "All databases initialized successfully!"
