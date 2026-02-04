#!/bin/bash
set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin}"
REALM="${REALM:-learnsmart}"
CLIENT_ID="${CLIENT_ID:-learnsmart-frontend}"

echo "Waiting for Keycloak to be ready..."
until curl -s "$KEYCLOAK_URL/realms/master" > /dev/null; do
  sleep 5
  echo -n "."
done
echo "Keycloak is up."

echo "Getting admin token..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=$ADMIN_USER" \
     -d "password=$ADMIN_PASS" \
     -d "grant_type=password" \
     -d "client_id=admin-cli" | jq -r .access_token)

if [ "$TOKEN" == "null" ]; then
  echo "Failed to get admin token"
  exit 1
fi

echo "Creating realm $REALM..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"realm\": \"$REALM\", \"enabled\": true}" || echo "Realm might already exist"

echo "Creating client $CLIENT_ID..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/clients" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{
       \"clientId\": \"$CLIENT_ID\",
       \"enabled\": true,
       \"publicClient\": true,
       \"directAccessGrantsEnabled\": true,
       \"standardFlowEnabled\": true,
       \"redirectUris\": [\"*\"],
       \"webOrigins\": [\"*\"]
     }" || echo "Client might already exist"

echo "Creating admin1 user..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{
       \"username\": \"admin1\",
       \"enabled\": true,
       \"requiredActions\": [],
       \"credentials\": [{\"type\": \"password\", \"value\": \"password\", \"temporary\": false}]
     }" || echo "User might already exist"

# We also need an 'ADMIN' role and assign it to admin1
echo "Creating ADMIN role..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/roles" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"ADMIN\"}" || echo "Role might already exist"

echo "Getting user ID for admin1..."
USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/users?username=admin1" \
     -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id')

echo "Ensuring no required actions and non-temporary password for admin1..."
curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM/users/$USER_ID" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"requiredActions\": [], \"emailVerified\": true}"

echo "Resetting password for admin1..."
curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM/users/$USER_ID/reset-password" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"type\": \"password\", \"value\": \"password\", \"temporary\": false}"

echo "Getting role ID for ADMIN..."
ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/roles/ADMIN" \
     -H "Authorization: Bearer $TOKEN" | jq -r '.id')

echo "Assigning ADMIN role to admin1..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users/$USER_ID/role-mappings/realm" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "[{\"id\": \"$ROLE_ID\", \"name\": \"ADMIN\"}]"

echo "Keycloak setup complete."
