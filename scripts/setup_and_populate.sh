#!/bin/bash
set -e

# Configuration
# Configuration
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:8762}"
REALM="${REALM:-learnsmart}"
CLIENT_ID="${CLIENT_ID:-learnsmart-frontend}"
USERNAME="${ADMIN_USERNAME:-admin1}"
PASSWORD="${ADMIN_PASSWORD:-password}"

echo "=================================================="
echo "   LearnSmart Setup & Population Script"
echo "=================================================="
echo ""
echo "This script will:"
echo "  1. Configure Keycloak (realm, client, users)"
echo "  2. Wait for services to be ready"
echo "  3. Populate sample content (domains, skills, content items)"
echo ""

# 1. Check Prerequisites
echo "[1] Checking dependencies..."
if ! command -v jq &> /dev/null; then
    echo "Installing jq..."
    sudo apt-get update && sudo apt-get install -y jq
fi

# 2. Configure Keycloak
echo ""
echo "[2] Configuring Keycloak..."
echo "Running setup_keycloak.sh..."

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Run Keycloak setup
bash scripts/setup_keycloak.sh

echo "✅ Keycloak configured."

# 3. Wait for Gateway
echo ""
echo "[3] Waiting for Gateway & Services..."
MAX_RETRIES=30
COUNT=0

until curl -s "$GATEWAY_URL/actuator/health" 2>/dev/null | grep -q "UP"; do
    echo "   Waiting for Gateway ($COUNT/$MAX_RETRIES)..."
    sleep 2
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "❌ Gateway not ready after 60s."
        echo "   Make sure docker-compose services are running:"
        echo "   docker-compose up -d"
        exit 1
    fi
done
echo "✅ Gateway is UP."

# 4. Get Token
echo ""
echo "[4] Authenticating as $USERNAME..."
TOKEN_RESP=$(curl -s -d "client_id=$CLIENT_ID" -d "username=$USERNAME" -d "password=$PASSWORD" -d "grant_type=password" \
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
        # May already exist
        echo "      ⚠️  Already exists (Status $STATUS)." >&2
        # Return empty, caller handles lookup
    else
        echo "      ⚠️  Status $STATUS. Response: $BODY" >&2
        # Don't exit, continue with what we have
    fi
}

# --- Domains ---
echo ""
echo "   📚 Creating Domains..."

# Domain 1: DevOps
DEVOPS_JSON='{
  "code": "DEVOPS",
  "name": "DevOps Engineering",
  "description": "Containerization, CI/CD, and Infrastructure as Code"
}'
DEVOPS_ID=$(create_resource "content/domains" "$DEVOPS_JSON" "Domain 'DevOps'")

if [ -z "$DEVOPS_ID" ]; then
     # Fetch existing
     FETCH=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/content/domains?code=DEVOPS")
     DEVOPS_ID=$(echo "$FETCH" | jq -r '.[0].id')
fi
echo "      → DevOps Domain ID: $DEVOPS_ID"

# Domain 2: Cloud Computing
CLOUD_JSON='{
  "code": "CLOUD",
  "name": "Cloud Computing",
  "description": "AWS, Azure, GCP fundamentals and best practices"
}'
CLOUD_ID=$(create_resource "content/domains" "$CLOUD_JSON" "Domain 'Cloud'")

if [ -z "$CLOUD_ID" ]; then
     FETCH=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$GATEWAY_URL/content/domains?code=CLOUD")
     CLOUD_ID=$(echo "$FETCH" | jq -r '.[0].id')
fi
echo "      → Cloud Domain ID: $CLOUD_ID"

# --- Skills ---
echo ""
echo "   🎯 Creating Skills..."

# Skill 1: Docker Basics
DOCKER_SKILL="{
  \"domainId\": \"$DEVOPS_ID\",
  \"code\": \"DOCKER_BASICS\",
  \"name\": \"Docker Basics\",
  \"description\": \"Containers, Images, and Volumes\",
  \"level\": \"BEGINNER\",
  \"tags\": [\"container\", \"docker\"]
}"
DOCKER_SKILL_ID=$(create_resource "content/skills" "$DOCKER_SKILL" "Skill 'Docker Basics'")
echo "      → Docker Skill ID: ${DOCKER_SKILL_ID:-existing}"

# Skill 2: Kubernetes
K8S_SKILL="{
  \"domainId\": \"$DEVOPS_ID\",
  \"code\": \"K8S_BASICS\",
  \"name\": \"Kubernetes Basics\",
  \"description\": \"Container Orchestration with K8s\",
  \"level\": \"INTERMEDIATE\",
  \"tags\": [\"kubernetes\", \"orchestration\"]
}"
K8S_SKILL_ID=$(create_resource "content/skills" "$K8S_SKILL" "Skill 'Kubernetes'")
echo "      → Kubernetes Skill ID: ${K8S_SKILL_ID:-existing}"

# Skill 3: AWS Fundamentals
AWS_SKILL="{
  \"domainId\": \"$CLOUD_ID\",
  \"code\": \"AWS_FUNDAMENTALS\",
  \"name\": \"AWS Fundamentals\",
  \"description\": \"EC2, S3, RDS, and IAM basics\",
  \"level\": \"BEGINNER\",
  \"tags\": [\"aws\", \"cloud\"]
}"
AWS_SKILL_ID=$(create_resource "content/skills" "$AWS_SKILL" "Skill 'AWS Fundamentals'")
echo "      → AWS Skill ID: ${AWS_SKILL_ID:-existing}"

# --- Content Items ---
echo ""
echo "   📝 Creating Content Items..."

# Content 1: Docker Intro
DOCKER_LESSON="{
  \"domainId\": \"$DEVOPS_ID\",
  \"type\": \"LESSON\",
  \"title\": \"Introduction to Docker\",
  \"description\": \"What are containers and why Docker matters\",
  \"estimatedMinutes\": 15,
  \"difficulty\": 0.2,
  \"isActive\": true,
  \"metadata\": {\"format\": \"markdown\"}
}"
create_resource "content/content-items" "$DOCKER_LESSON" "Lesson 'Docker Intro'" > /dev/null

# Content 2: Docker Volumes
DOCKER_VOLUMES="{
  \"domainId\": \"$DEVOPS_ID\",
  \"type\": \"TUTORIAL\",
  \"title\": \"Working with Docker Volumes\",
  \"description\": \"Persistent data in containerized applications\",
  \"estimatedMinutes\": 30,
  \"difficulty\": 0.4,
  \"isActive\": true,
  \"metadata\": {\"format\": \"interactive\"}
}"
create_resource "content/content-items" "$DOCKER_VOLUMES" "Tutorial 'Docker Volumes'" > /dev/null

# Content 3: Kubernetes Pods
K8S_PODS="{
  \"domainId\": \"$DEVOPS_ID\",
  \"type\": \"LESSON\",
  \"title\": \"Kubernetes Pods Explained\",
  \"description\": \"Understanding the smallest deployable unit in K8s\",
  \"estimatedMinutes\": 20,
  \"difficulty\": 0.5,
  \"isActive\": true,
  \"metadata\": {\"format\": \"video\"}
}"
create_resource "content/content-items" "$K8S_PODS" "Lesson 'K8s Pods'" > /dev/null

# Content 4: AWS EC2
AWS_EC2="{
  \"domainId\": \"$CLOUD_ID\",
  \"type\": \"LESSON\",
  \"title\": \"AWS EC2 Fundamentals\",
  \"description\": \"Launch and manage virtual servers in the cloud\",
  \"estimatedMinutes\": 25,
  \"difficulty\": 0.3,
  \"isActive\": true,
  \"metadata\": {\"format\": \"markdown\"}
}"
create_resource "content/content-items" "$AWS_EC2" "Lesson 'AWS EC2'" > /dev/null

echo ""
echo "=================================================="
echo "✅ Full Setup & Population Complete"
echo "=================================================="
echo ""
echo "Summary:"
echo "  📚 Domains:       DevOps Engineering, Cloud Computing"
echo "  🎯 Skills:        Docker, Kubernetes, AWS"
echo "  📝 Content Items: 4 items (lessons & tutorials)"
echo ""
echo "Next Steps:"
echo "  • Test the API: curl http://localhost:8762/content/domains"
echo "  • Run simulation: python3 scripts/simulate_react_learning.py"
echo "  • Create a user and start learning!"
echo ""
