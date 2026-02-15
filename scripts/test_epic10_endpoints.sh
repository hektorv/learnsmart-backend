#!/bin/bash
# Test script for Epic 10 endpoints

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8762}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
REALM="${REALM:-learnsmart}"
ADMIN_USER="${ADMIN_USER:-admin1}"
ADMIN_PASS="${ADMIN_PASS:-password}"
CLIENT_ID="${CLIENT_ID:-learnsmart-frontend}"

echo "=== Epic 10 Endpoint Testing ==="
echo ""

# Get token
echo "1. Getting admin token..."
TOKEN_RESPONSE=$(curl -s -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${ADMIN_USER}" \
  -d "password=${ADMIN_PASS}" \
  -d "grant_type=password")

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ Failed to get token"
  echo "Response: $TOKEN_RESPONSE"
  exit 1
fi

echo "✓ Token obtained"
echo ""

# Get or create domain
echo "2. Getting domain..."
DOMAIN_RESPONSE=$(curl -s -X GET \
  "${GATEWAY_URL}/content/domains?code=react-dev" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

DOMAIN_ID=$(echo $DOMAIN_RESPONSE | jq -r '.[0].id // .content[0].id')

if [ "$DOMAIN_ID" == "null" ] || [ -z "$DOMAIN_ID" ]; then
  echo "  Creating domain..."
  DOMAIN_CREATE=$(curl -s -X POST \
    "${GATEWAY_URL}/content/domains" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"code":"react-dev","name":"React Development","description":"Master React"}')
  DOMAIN_ID=$(echo $DOMAIN_CREATE | jq -r '.id')
fi

echo "✓ Domain ID: $DOMAIN_ID"
echo ""

# Test US-10-06: Generate Skills
echo "3. Testing US-10-06: Generate Skills (AI)..."
echo "   URL: ${GATEWAY_URL}/content/skills/domains/${DOMAIN_ID}/generate"

GENERATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST \
  "${GATEWAY_URL}/content/skills/domains/${DOMAIN_ID}/generate" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"topic":"React Hooks"}')

HTTP_STATUS=$(echo "$GENERATE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$GENERATE_RESPONSE" | sed '/HTTP_STATUS/d')

echo "   Status: $HTTP_STATUS"
if [ "$HTTP_STATUS" == "201" ] || [ "$HTTP_STATUS" == "500" ]; then
  echo "   ✓ Endpoint is reachable (implementation pending)"
elif [ "$HTTP_STATUS" == "404" ]; then
  echo "   ❌ 404 Not Found - Endpoint not registered"
  echo "   Response: $BODY"
else
  echo "   ⚠️  Unexpected status: $HTTP_STATUS"
  echo "   Response: $BODY"
fi
echo ""

# Test US-10-07: Link Prerequisites
echo "4. Testing US-10-07: Link Skills Prerequisites (AI)..."
echo "   URL: ${GATEWAY_URL}/content/skills/domains/${DOMAIN_ID}/link"

LINK_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST \
  "${GATEWAY_URL}/content/skills/domains/${DOMAIN_ID}/link" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

HTTP_STATUS=$(echo "$LINK_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$LINK_RESPONSE" | sed '/HTTP_STATUS/d')

echo "   Status: $HTTP_STATUS"
if [ "$HTTP_STATUS" == "204" ] || [ "$HTTP_STATUS" == "500" ]; then
  echo "   ✓ Endpoint is reachable (implementation pending)"
elif [ "$HTTP_STATUS" == "404" ]; then
  echo "   ❌ 404 Not Found - Endpoint not registered"
  echo "   Response: $BODY"
else
  echo "   ⚠️  Unexpected status: $HTTP_STATUS"
  echo "   Response: $BODY"
fi
echo ""

echo "=== Test Complete ==="
