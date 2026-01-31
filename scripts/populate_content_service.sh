#!/bin/bash

# Configuration
GATEWAY_URL="http://localhost:8762"
KEYCLOAK_URL="http://localhost:8080"
REALM="learnsmart"
CLIENT_ID="api-gateway"
CLIENT_SECRET="R5dZSvGD8y6diOgI9QRPM3PHaBR8tpDa" 

# User Credentials (using student1 for simplicity as it has valid access)
EMAIL="student1@example.com"
PASSWORD="password"

# Ensure jq is installed
if ! command -v jq &> /dev/null; then
    echo "jq could not be found"
    exit 1
fi

echo "--------------------------------------------------"
echo "Populating Content Service via Gateway"
echo "--------------------------------------------------"

# 1. Authenticate
echo "[1] Authenticating..."
TOKEN_RESPONSE=$(curl -s -d "client_id=$CLIENT_ID" -d "client_secret=$CLIENT_SECRET" -d "username=$EMAIL" -d "password=$PASSWORD" -d "grant_type=password" \
  "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r .access_token)

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to get token."
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi
echo "✅ Token obtained."

# 2. Create Domain (DevOps)
echo "[2] Creating Domain: DevOps..."
DOMAIN_JSON='{
  "code": "DEVOPS",
  "name": "DevOps Engineering",
  "description": "Containerization and CI/CD"
}'

DOMAIN_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$DOMAIN_JSON" \
  "$GATEWAY_URL/domains")

HTTP_STATUS=$(echo "$DOMAIN_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$DOMAIN_RESP" | sed 's/HTTP_STATUS:.*//')

if [ "$HTTP_STATUS" == "201" ]; then
    echo "✅ Domain Created."
    DOMAIN_ID=$(echo "$BODY" | jq -r .id)
    echo "   ID: $DOMAIN_ID"
else
    # Check if it already exists (conflict/duplicates handled via database constraints usually return 500 or 409)
    echo "⚠️ Failed/Existed? Status: $HTTP_STATUS"
    # Try to fetch it to proceed
    FETCH_RESP=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/domains?code=DEVOPS")
    DOMAIN_ID=$(echo "$FETCH_RESP" | jq -r '.content[0].id')
    if [ "$DOMAIN_ID" != "null" ]; then
         echo "   recovered ID from existing: $DOMAIN_ID"
    else
         echo "   ❌ Could not recover Domain ID. Aborting." 
         exit 1
    fi
fi

# 3. Create Skill (Docker)
echo "[3] Creating Skill: Docker..."
SKILL_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"code\": \"DOCKER_BASICS\",
  \"name\": \"Docker Basics\",
  \"description\": \"Images, Containers, Volumes\",
  \"level\": \"BEGINNER\",
  \"tags\": [\"container\", \"infrastructure\"]
}"

SKILL_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$SKILL_JSON" \
  "$GATEWAY_URL/skills")

HTTP_STATUS=$(echo "$SKILL_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
echo "   Status: $HTTP_STATUS"

# 4. Create Content (Lesson)
echo "[4] Creating Content: Docker Intro Lesson..."
CONTENT_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"type\": \"LESSON\",
  \"title\": \"Introduction to Docker\",
  \"description\": \"What is a container?\",
  \"estimatedMinutes\": 10,
  \"difficulty\": 0.1,
  \"isActive\": true,
  \"metadata\": { \"format\": \"markdown\" }
}"

CONTENT_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$CONTENT_JSON" \
  "$GATEWAY_URL/content-items")

HTTP_STATUS=$(echo "$CONTENT_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
if [ "$HTTP_STATUS" == "201" ]; then
     echo "✅ Content Created."
else
     echo "❌ Content Creation Failed. Status: $HTTP_STATUS"
     echo "   Response: $(echo "$CONTENT_RESP" | sed 's/HTTP_STATUS:.*//')"
fi

echo "--------------------------------------------------"
echo "Population Complete"
echo "--------------------------------------------------"
