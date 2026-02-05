#!/bin/bash

# Deployment Script for LearnSmart Backend (Oracle Cloud / Ubuntu / Debian)
# Usage: ./deploy.sh

set -e

echo "🚀 Starting Deployment..."

# 1. Update System & Install Dependencies
echo "📦 Updating system packages..."
if [ -f /etc/redhat-release ]; then
    # Oracle Linux / RHEL / CentOS
    sudo dnf update -y
    sudo dnf config-manager --add-repo=https://download.docker.com/linux/centos/docker-ce.repo
    sudo dnf install -y docker-ce docker-ce-cli containerd.io git
elif [ -f /etc/debian_version ]; then
    # Ubuntu / Debian
    sudo apt-get update
    sudo apt-get install -y ca-certificates curl gnupg
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg
    echo \
      "deb [arch=\"$(dpkg --print-architecture)\" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" | \
      sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin git
fi

# 2. Start Docker
echo "🐳 Starting Docker..."
sudo systemctl enable --now docker
sudo usermod -aG docker $USER

# 3. Check for Project
if [ ! -f "docker-compose.prod.yml" ]; then
    echo "❌ docker-compose.prod.yml not found!"
    echo "Please clone the repo or upload the files first."
    exit 1
fi

# 4. Run Docker Compose
echo "🚀 Building and Starting Services..."
# Ensure script is executable
chmod +x scripts/init-shared-db.sh

# Stop existing if any
sudo docker compose -f docker-compose.prod.yml down --remove-orphans || true

# Start new
sudo docker compose -f docker-compose.prod.yml up -d --build

echo "✅ Deployment Complete! Services are starting up."
echo "Check status with: docker compose -f docker-compose.prod.yml logs -f"
