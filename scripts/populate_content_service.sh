#!/bin/bash

# Configuration
GATEWAY_URL="http://localhost:8762"
KEYCLOAK_URL="http://localhost:8080"
REALM="learnsmart"
CLIENT_ID="learnsmart-frontend"  # Public client

# Admin Credentials (created by setup_keycloak.sh)
USERNAME="admin1"
PASSWORD="password"

# Ensure jq is installed
if ! command -v jq &> /dev/null; then
    echo "jq could not be found. Installing..."
    sudo apt-get update && sudo apt-get install -y jq
fi

echo "--------------------------------------------------"
echo "Populating Content Service with Sample Data"
echo "--------------------------------------------------"

# 1. Authenticate
echo "[1] Authenticating as $USERNAME..."
TOKEN_RESPONSE=$(curl -s -d "client_id=$CLIENT_ID" -d "username=$USERNAME" -d "password=$PASSWORD" -d "grant_type=password" \
  "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r .access_token)

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to get token."
    echo "Response: $TOKEN_RESPONSE"
    echo ""
    echo "HINT: Make sure Keycloak is running and configured."
    echo "Run: ./scripts/setup_keycloak.sh"
    exit 1
fi
echo "✅ Token obtained."

# 2. Create Domain (DevOps)
echo "[2] Creating Domain: DevOps Engineering..."
DOMAIN_JSON='{
  "code": "DEVOPS",
  "name": "DevOps Engineering",
  "description": "Containerization, CI/CD, and Infrastructure as Code"
}'

DOMAIN_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$DOMAIN_JSON" \
  "$GATEWAY_URL/content/domains")

HTTP_STATUS=$(echo "$DOMAIN_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$DOMAIN_RESP" | sed 's/HTTP_STATUS:.*//')

if [ "$HTTP_STATUS" == "201" ]; then
    echo "✅ Domain Created."
    DOMAIN_ID=$(echo "$BODY" | jq -r .id)
    echo "   ID: $DOMAIN_ID"
else
    # Try to fetch existing domain
    echo "⚠️  Status: $HTTP_STATUS (may already exist)"
    FETCH_RESP=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/content/domains/DEVOPS")
    DOMAIN_ID=$(echo "$FETCH_RESP" | jq -r '.id')
    if [ "$DOMAIN_ID" != "null" ] && [ -n "$DOMAIN_ID" ]; then
         echo "   ✅ Recovered existing Domain ID: $DOMAIN_ID"
    else
         echo "   ❌ Could not create or recover Domain. Response:"
         echo "$BODY"
         exit 1
    fi
fi

# 3. Create Skill (Docker Basics)
echo "[3] Creating Skill: Docker Basics..."
SKILL_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"code\": \"DOCKER_BASICS\",
  \"name\": \"Docker Basics\",
  \"description\": \"Learn the fundamentals of Docker: Images, Containers, and Volumes\",
  \"level\": \"BEGINNER\",
  \"tags\": [\"container\", \"infrastructure\", \"docker\"]
}"

SKILL_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$SKILL_JSON" \
  "$GATEWAY_URL/content/skills")

HTTP_STATUS=$(echo "$SKILL_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$SKILL_RESP" | sed 's/HTTP_STATUS:.*//')

if [ "$HTTP_STATUS" == "201" ]; then
    echo "✅ Skill Created."
    SKILL_ID=$(echo "$BODY" | jq -r .id)
    echo "   ID: $SKILL_ID"
elif [ "$HTTP_STATUS" == "409" ]; then
    echo "⚠️  Skill already exists (Status: 409)"
else
    echo "⚠️  Status: $HTTP_STATUS"
    echo "   Response: $BODY"
fi

# 4. Create Skill (Kubernetes Basics)
echo "[4] Creating Skill: Kubernetes Basics..."
K8S_SKILL_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"code\": \"K8S_BASICS\",
  \"name\": \"Kubernetes Basics\",
  \"description\": \"Container orchestration with Kubernetes: Pods, Services, and Deployments\",
  \"level\": \"INTERMEDIATE\",
  \"tags\": [\"kubernetes\", \"orchestration\", \"k8s\"]
}"

K8S_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$K8S_SKILL_JSON" \
  "$GATEWAY_URL/content/skills")

HTTP_STATUS=$(echo "$K8S_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
if [ "$HTTP_STATUS" == "201" ]; then
    echo "✅ Skill Created."
elif [ "$HTTP_STATUS" == "409" ]; then
    echo "⚠️  Skill already exists"
else
    echo "⚠️  Status: $HTTP_STATUS"
fi

# 5. Create Content Item (Docker Intro Lesson)
echo "[5] Creating Content: Introduction to Docker..."
CONTENT_JSON="{
  \"domainId\": \"$DOMAIN_ID\",
  \"type\": \"LESSON\",
  \"title\": \"Introduction to Docker\",
  \"description\": \"Understand what containers are and why Docker revolutionized application deployment\",
  \"estimatedMinutes\": 15,
  \"difficulty\": 0.2,
  \"isActive\": true,
  \"metadata\": { 
    \"format\": \"markdown\",
    \"objectives\": [\"Understand containerization\", \"Learn Docker basics\"]
  }
}"

CONTENT_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$CONTENT_JSON" \
  "$GATEWAY_URL/content/content-items")

HTTP_STATUS=$(echo "$CONTENT_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
if [ "$HTTP_STATUS" == "201" ]; then
     echo "✅ Content Created."
     CONTENT_ID=$(echo "$CONTENT_RESP" | sed 's/HTTP_STATUS:.*//' | jq -r .id)
     echo "   ID: $CONTENT_ID"
elif [ "$HTTP_STATUS" == "409" ]; then
     echo "⚠️  Content already exists"
else
     echo "⚠️  Content Creation Status: $HTTP_STATUS"
     echo "   Response: $(echo "$CONTENT_RESP" | sed 's/HTTP_STATUS:.*//')"
fi

# 6. Create Content Item (Docker Volumes Tutorial)
echo "[6] Creating Content: Docker Volumes Tutorial..."
VOLUME_CONTENT="{
  \"domainId\": \"$DOMAIN_ID\",
  \"type\": \"TUTORIAL\",
  \"title\": \"Working with Docker Volumes\",
  \"description\": \"Hands-on tutorial for managing persistent data in Docker containers\",
  \"estimatedMinutes\": 30,
  \"difficulty\": 0.4,
  \"isActive\": true,
  \"metadata\": { 
    \"format\": \"interactive\",
    \"prerequisites\": [\"DOCKER_BASICS\"]
  }
}"

VOL_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d "$VOLUME_CONTENT" \
  "$GATEWAY_URL/content/content-items")

HTTP_STATUS=$(echo "$VOL_RESP" | grep "HTTP_STATUS" | cut -d: -f2)
if [ "$HTTP_STATUS" == "201" ]; then
     echo "✅ Content Created."
elif [ "$HTTP_STATUS" == "409" ]; then
     echo "⚠️  Content already exists"
else
     echo "⚠️  Status: $HTTP_STATUS"
fi

echo ""
echo "--------------------------------------------------"
echo "✅ Population Complete"
echo "--------------------------------------------------"
echo ""
echo "Summary:"
echo "  • Domain: DevOps Engineering"
echo "  • Skills: Docker Basics, Kubernetes Basics"
echo "  • Content: 2 items (Intro lesson, Volumes tutorial)"
echo ""
echo "You can now use these resources for testing and creating learning plans!"
