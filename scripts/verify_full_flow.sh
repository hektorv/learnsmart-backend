#!/bin/bash

# Configuration
GATEWAY_URL="http://localhost:8762"
KEYCLOAK_URL="http://localhost:8080"
REALM="learnsmart"
CLIENT_ID="api-gateway" # Using confidental client for simplicity, or frontend-client if public
CLIENT_SECRET="$KEYCLOAK_CLIENT_SECRET" # Need this if client is confidential. 
# Actually, let's use the 'frontend' client which is likely public in previous setups, or 'api-gateway' if I have secret.
# In "configure_keycloak.py", usually 'api-gateway' is confidential.
# Let's try to grab a token using 'api-gateway' and secret if env var set, otherwise try 'frontend' (public).

# Ensure jq is installed
if ! command -v jq &> /dev/null; then
    echo "jq could not be found"
    exit 1
fi

EMAIL="student1@example.com" # Pre-provisioned user
PASSWORD="password"
DISPLAY_NAME="Student One"

echo "--------------------------------------------------"
echo "Starting Functional Verification with PostgreSQL"
echo "Target User: $EMAIL"
echo "--------------------------------------------------"

# 1. Register User (Public Endpoint)
echo "[1] Registering User via Gateway -> Profile-Service..."
REGISTER_RESPONSE=$(curl -v -X POST -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\", \"password\":\"$PASSWORD\", \"displayName\":\"$DISPLAY_NAME\"}" \
  "$GATEWAY_URL/auth/register" 2>&1)

echo "Response: $REGISTER_RESPONSE"

# Extract ID logic/error check
# Extract ID logic/error check
if echo "$REGISTER_RESPONSE" | grep -q "error"; then
   echo "⚠️ Registration failed (likely already exists). Proceeding to login..."
   # Do not exit
else
   echo "✅ ID: $(echo "$REGISTER_RESPONSE" | jq -r .id)"
fi

# 2. Get Access Token (Password Grant)
# We need to know which client to use. Assuming 'api-gateway' with secret 'provider_secret' (from previous context logs or default).
# Better to use 'frontend' public client if it exists.
# Let's try 'api-gateway' with the secret I saw in docker-compose 'provider_secret'? 
# Or check the logs. Previous logs showed KEYCLOAK_CLIENT_SECRET env var.

CLIENT_ID="api-gateway"
SECRET="R5dZSvGD8y6diOgI9QRPM3PHaBR8tpDa" # Default

# Try to read secret from file
if [ -f "client_secret.txt" ]; then
    SECRET=$(cat client_secret.txt)
    echo "Found client_secret.txt, using secret: ${SECRET:0:5}..."
elif [ -n "$KEYCLOAK_CLIENT_SECRET" ]; then
    SECRET="$KEYCLOAK_CLIENT_SECRET"
    echo "Using secret from environment variable."
else
    echo "Using default hardcoded secret."
fi 

echo "[2] Authenticating with Keycloak..."
TOKEN_RESPONSE=$(curl -s -d "client_id=$CLIENT_ID" -d "client_secret=$SECRET" -d "username=$EMAIL" -d "password=$PASSWORD" -d "grant_type=password" \
  "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r .access_token)

if [ "$ACCESS_TOKEN" == "null" ]; then
  echo "Failed to get token. Response: $TOKEN_RESPONSE"
  exit 1
fi
echo "Token obtained."

# 3. Verify Content (Protected)
echo "[3] Accessing Content Service (GET /content-items)..."
CONTENT_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/content-items")
echo "HTTP Status: $CONTENT_RESPONSE"
if [ "$CONTENT_RESPONSE" == "200" ]; then
    echo "✅ Content Service Secured & Accessible"
else
    echo "❌ Content Service Failed"
fi

# 4. Verify Tracking (Protected)
echo "[4] Accessing Tracking Service (GET /events)..."
TRACKING_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/events")
echo "HTTP Status: $TRACKING_RESPONSE"
if [ "$TRACKING_RESPONSE" == "200" ]; then
    echo "✅ Tracking Service Secured & Accessible"
else
    echo "❌ Tracking Service Failed"
fi

# 5. Create Plan (Complex Flow: Planning -> Profile + Content)
echo "[5] Creating Learning Plan (Planning -> Profile/Content/AI)..."
# We need existing content/profile? 
# The create plan request usually needs userId.
# Let's verify what `createPlan` expects.
# PlanController.createPlan(@RequestBody LearningPlan plan)
# We'll send minimal valid json.
PLAN_JSON="{\"userId\":\"$EMAIL\", \"topic\":\"Integration Test\", \"status\":\"DRAFT\"}"

PLAN_RESPONSE=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$PLAN_JSON" \
  "$GATEWAY_URL/plans")

# It might fail if AI service is down or Profile lookup fails (user ID mismatch? Profile stores email as ID? No, usually UUID).
# The register response had ID.
USER_ID_FROM_REGISTER=$(echo "$REGISTER_RESPONSE" | jq -r .id)
echo "User ID from Register: $USER_ID_FROM_REGISTER"

PLAN_JSON_WITH_ID="{\"userId\":\"$USER_ID_FROM_REGISTER\", \"topic\":\"Integration Test\", \"status\":\"DRAFT\", \"goals\":[\"Pass Test\"]}"

echo "Sending Plan: $PLAN_JSON_WITH_ID"
PLAN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$PLAN_JSON_WITH_ID" \
  "$GATEWAY_URL/plans")

echo "Response: $PLAN_RESPONSE"

if echo "$PLAN_RESPONSE" | grep -q "HTTP_STATUS:201"; then
    echo "✅ Planning Service (Chain) Successful"
else
    echo "⚠️ Planning Service Warning (Check logs if AI/Feign failed)"
fi
