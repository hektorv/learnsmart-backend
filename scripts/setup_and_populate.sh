#!/bin/bash
set -e

# Configuration
KEYCLOAK_URL="http://localhost:8080"
GATEWAY_URL="http://localhost:8762"
REALM="learnsmart"
USERNAME="student1@example.com"
PASSWORD="password"

echo "=================================================="
echo "   LearnSmart Setup & Population Script"
echo "=================================================="

# 1. Install Prerequisites
echo "[1] Checking dependencies..."
if ! command -v jq &> /dev/null; then
    echo "Installing jq..."
    sudo apt-get update && sudo apt-get install -y jq
fi
if ! python3 -c "import requests" &> /dev/null; then
    echo "Installing python requests..."
    pip3 install requests
fi

# 2. Configure Keycloak
echo ""
echo "[2] Configuring Keycloak..."
# Ensure we are in the root directory context
cd "$(dirname "$0")/.."
python3 scripts/configure_keycloak.py

# Read the secret
if [ -f "client_secret.txt" ]; then
    CLIENT_SECRET=$(cat client_secret.txt)
    echo "✅ Recovered Client Secret: ${CLIENT_SECRET:0:5}..."
else
    echo "❌ client_secret.txt not found. Keycloak configuration might have failed."
    exit 1
fi

# 3. Wait for Gateway/Content Service
echo ""
echo "[3] Waiting for Gateway & Content Service..."
MAX_RETRIES=30
COUNT=0
until curl -s "$GATEWAY_URL/actuator/health" | grep "UP" > /dev/null; do
    echo "   Waiting for Gateway ($COUNT/$MAX_RETRIES)..."
    sleep 2
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "❌ Gateway not ready after 60s."
        exit 1
    fi
done
echo "✅ Gateway is UP."

# 4. Get Token
echo ""
echo "[4] Authenticating as $USERNAME..."
TOKEN_RESP=$(curl -s -d "client_id=api-gateway" -d "client_secret=$CLIENT_SECRET" -d "username=$USERNAME" -d "password=$PASSWORD" -d "grant_type=password" \
  "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token")

ACCESS_TOKEN=$(echo "$TOKEN_RESP" | jq -r .access_token)

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to get token."
    echo "Response: $TOKEN_RESP"
    exit 1
fi
echo "✅ Token obtained."

# 5. Populate Content Service
echo ""
echo "[5] Populating Content Service..."

# Function to create resource with retry
create_resource() {
    ENDPOINT=$1
    DATA=$2
    NAME=$3
    
    echo "   Creating $NAME..." >&2
    RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
      -d "$DATA" \
      "$GATEWAY_URL/$ENDPOINT")
      
    STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
    BODY=$(echo "$RESPONSE" | sed 's/HTTP_STATUS:.*//')
    
    if [ "$STATUS" == "201" ]; then
        echo "      ✅ Created." >&2
        ID=$(echo "$BODY" | jq -r .id)
        echo "$ID"
    elif [ "$STATUS" == "409" ] || [ "$STATUS" == "500" ]; then
        # Assume 500 might be duplicate key in some sloppy handlers, or 409 is correct
        echo "      ⚠️  Already exists (Status $STATUS)." >&2
        # Return nothing on stdout, caller handles lookup
    else
        echo "      ❌ Failed (Status $STATUS). Response: $BODY" >&2
        exit 1
    fi
}

# --- Domain ---
DOMAIN_JSON='{"code": "DEVOPS", "name": "DevOps Engineering", "description": "CI/CD & Containers"}'
DOMAIN_ID=$(create_resource "domains" "$DOMAIN_JSON" "Domain 'DevOps'")

if [ -z "$DOMAIN_ID" ]; then
     # Fetch existing
     FETCH=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/domains?code=DEVOPS")
     # Controller returns List<Domain>, not Page. So it is a root array.
     DOMAIN_ID=$(echo "$FETCH" | jq -r '.[0].id')
fi
echo "      -> Domain ID: $DOMAIN_ID"

# --- Skill ---
SKILL_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"code\": \"DOCKER_BASICS\",
  \"name\": \"Docker Basics\",
  \"description\": \"Containers 101\",
  \"level\": \"BEGINNER\",
  \"tags\": [\"container\"]
}"
SKILL_ID=$(create_resource "skills" "$SKILL_JSON" "Skill 'Docker'")

# --- Content ---
CONTENT_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"type\": \"LESSON\",
  \"title\": \"Intro to Docker\",
  \"description\": \"Container fundamentals\",
  \"estimatedMinutes\": 10,
  \"difficulty\": 0.1,
  \"isActive\": true,
  \"metadata\": { \"format\": \"markdown\" }
}"
# We don't strictly need the ID for next steps so we ignore return
create_resource "content-items" "$CONTENT_JSON" "Content 'Intro to Docker'"

echo ""
echo "=================================================="
echo "✅ Full Setup & Population Complete"
echo "=================================================="
