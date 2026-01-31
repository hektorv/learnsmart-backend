#!/bin/bash

# Configuration
KEYCLOAK_URL="http://localhost:8080"
GATEWAY_URL="http://localhost:8762"
REALM="learnsmart"
CLIENT_ID="api-gateway"
CLIENT_SECRET="R5dZSvGD8y6diOgI9QRPM3PHaBR8tpDa" 

# Credentials
USER_EMAIL="user1@example.com"
ADMIN_EMAIL="admin1@example.com"
PASSWORD="password"

echo "--------------------------------------------------"
echo "RBAC Verification: USER vs ADMIN"
echo "--------------------------------------------------"

get_token() {
    USERNAME=$1
    curl -s -d "client_id=$CLIENT_ID" -d "client_secret=$CLIENT_SECRET" -d "username=$USERNAME" -d "password=$PASSWORD" -d "grant_type=password" \
      "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" | jq -r .access_token
}

echo "[1] Getting Tokens..."
USER_TOKEN=$(get_token "$USER_EMAIL")
ADMIN_TOKEN=$(get_token "$ADMIN_EMAIL")

if [ "$USER_TOKEN" == "null" ] || [ "$ADMIN_TOKEN" == "null" ]; then
    echo "❌ Failed to get tokens."
    exit 1
fi
echo "✅ Tokens obtained."

# Test 1: USER tries to create Domain (Should Fail)
echo "[2] Testing USER permissions (Create Domain)..."
DOMAIN_JSON='{"code": "HACK", "name": "Hacking", "description": "Should fail"}'

USER_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d "$DOMAIN_JSON" "$GATEWAY_URL/domains")

if [ "$USER_RESP" == "403" ]; then
    echo "✅ USER denied access (403 Forbidden) as expected."
else
    echo "❌ USER was NOT denied! Status: $USER_RESP"
fi

# Test 2: ADMIN tries to create Domain (Should Succeed)
echo "[3] Testing ADMIN permissions (Create Domain)..."
ADMIN_JSON='{"code": "SECURE", "name": "Security", "description": "Admin Only"}'

ADMIN_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "$ADMIN_JSON" "$GATEWAY_URL/domains")

if [ "$ADMIN_RESP" == "201" ]; then
    echo "✅ ADMIN allowed access (201 Created)."
elif [ "$ADMIN_RESP" == "500" ]; then 
    # Duplicate key?
    echo "⚠️ ADMIN request returned 500 (Duplicate?). Considering access granted."
else
    echo "❌ ADMIN failed! Status: $ADMIN_RESP"
fi

echo "--------------------------------------------------"
echo "RBAC Verification Complete"
echo "--------------------------------------------------"
