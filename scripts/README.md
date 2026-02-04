# Scripts Directory - Usage Guide

This directory contains utility scripts for setting up, populating, and testing the LearnSmart platform.

## 🚀 Quick Start Scripts

### 1. `setup_keycloak.sh` - Keycloak Configuration

**Purpose**: Configure Keycloak with the required realm, client, and users.

**Usage**:
```bash
./scripts/setup_keycloak.sh
```

**What it does**:
- Creates `learnsmart` realm
- Creates `learnsmart-frontend` public client
- Creates `ADMIN` and `STUDENT` roles
- Creates `admin1` user with ADMIN role (username: `admin1`, password: `password`)

**When to use**:
- After recreating Docker volumes (`docker-compose down -v`)
- First time setup
- After Keycloak database reset

---

### 2. `populate_content_service.sh` - Sample Content Population

**Purpose**: Populate the content service with sample domains, skills, and content items for testing.

**Prerequisites**:
- Keycloak configured (run `setup_keycloak.sh` first)
- Services running (`docker-compose up -d`)

**Usage**:
```bash
./scripts/populate_content_service.sh
```

**What it creates**:
- **Domain**: DevOps Engineering
- **Skills**: Docker Basics, Kubernetes Basics
- **Content**: 2 learning items (intro lesson, volumes tutorial)

**Use case**: Quick manual testing of content endpoints

---

### 3. `setup_and_populate.sh` - Complete Setup (All-in-One)

**Purpose**: Complete setup from scratch - configure Keycloak + populate sample content.

**Prerequisites**:
- Services running (`docker-compose up -d`)

**Usage**:
```bash
./scripts/setup_and_populate.sh
```

**What it does**:
1. Checks and installs dependencies (jq)
2. Runs `setup_keycloak.sh`
3. Waits for services to be ready
4. Populates sample content:
   - **Domains**: DevOps Engineering, Cloud Computing
   - **Skills**: Docker Basics, Kubernetes, AWS Fundamentals
   - **Content**: 4 learning items across different types

**Use case**: First-time setup or complete system reset

---

## 🧪 Testing Scripts

### 4. `simulate_react_learning.py` - End-to-End Simulation

**Purpose**: Comprehensive end-to-end validation of all User Stories.

**Prerequisites**:
- All services running
- Keycloak configured

**Usage**:
```bash
python3 scripts/simulate_react_learning.py
```

**What it validates**:
- ✅ US-110: Activity Completion Timestamps
- ✅ US-094: User Audit Trail
- ✅ US-107: Automatic Replanning Triggers
- ✅ US-096: Goal Completion Tracking
- ✅ US-123: Event Payload Validation
- ✅ US-111: Skill Prerequisite Validation
- ✅ US-093: Skill Validation (Profile service)
- ✅ US-0115: Assessment Item Deduplication

**Output**: Complete simulation log with test results

---

### 5. `simulate_classroom_load.py` - Load & Robustness Test

**Purpose**: Simulates multiple concurrent students to verify system stability under load.

**Prerequisites**:
- All services running
- Keycloak configured

**Usage**:
```bash
python3 scripts/simulate_classroom_load.py
```

**Features**:
- 🚀 **Concurrency**: Runs 5 parallel student sessions (configurable).
- 🔄 **Isolation**: Generates unique users and data for each thread to avoid collisions.
- 🛡️ **Robustness**: Validates that no race conditions or locks freeze the system during high activity.

**Output**: Pass/Fail summary after all sessions complete.

---

### 5. `security_validation.py` - AI Service Security Audit

**Purpose**: Validate security configurations of the AI service.

**Usage**:
```bash
python3 scripts/security_validation.py
```

---

### 6. `verify_deployment.py` - Health Check

**Purpose**: Quick health check for all services.

**Usage**:
```bash
python3 scripts/verify_deployment.py
```

---

## 📋 Common Workflows

### Fresh Start (After `docker-compose down -v`)

```bash
# 1. Start services
docker-compose up -d

# 2. Wait for services (3-5 minutes for Keycloak)
sleep 180

# 3. Run complete setup
./scripts/setup_and_populate.sh

# 4. Verify with simulation
python3 scripts/simulate_react_learning.py
```

### Just Reset Content (Keep Keycloak)

```bash
# Just repopulate content
./scripts/populate_content_service.sh
```

### Just Reset Keycloak (Keep Content)

```bash
# Just reconfigure Keycloak
./scripts/setup_keycloak.sh
```

---

## 🔑 Default Credentials

Created by `setup_keycloak.sh`:

| User     | Password   | Role   | Purpose              |
|----------|------------|--------|----------------------|
| admin1   | password   | ADMIN  | Admin/testing user   |

**Client Configuration**:
- **Client ID**: `learnsmart-frontend`
- **Type**: Public (no client secret)
- **Grant Types**: `password` (direct access), `authorization_code`

---

## 🛠️ Troubleshooting

### "Failed to get token"
- **Cause**: Keycloak not ready or not configured
- **Solution**: 
  ```bash
  # Check if Keycloak is up
  curl http://localhost:8080/realms/learnsmart
  
  # If 404, run setup
  ./scripts/setup_keycloak.sh
  ```

### "Gateway not ready"
- **Cause**: Services still starting
- **Solution**: Wait longer (up to 3 minutes) or check logs:
  ```bash
  docker-compose logs gateway
  ```

### "Domain already exists" warnings
- **Status**: Normal - script handles duplicates gracefully
- **Action**: None required

---

## 📁 Script Dependencies

```
setup_and_populate.sh
├── setup_keycloak.sh
└── (creates content via API)

populate_content_service.sh
└── (requires Keycloak already configured)

simulate_react_learning.py
└── (requires both Keycloak + content configured)
```

---

## 🗑️ Deprecated Scripts

The following scripts are **deprecated** and should not be used:

- `configure_keycloak.py` → **Use**: `setup_keycloak.sh`
- `simulation_final_backend_v2.py` → **Use**: `simulate_react_learning.py`
- `simulation_final_student_validation.py` → **Use**: `simulate_react_learning.py`
- `simulation_gap_filler.py` → **Use**: `simulate_react_learning.py`
- `simulation_read_after_write_v3.py` → **Use**: `simulate_react_learning.py`
- `simulation_verbose_validation_v2.py` → **Use**: `simulate_react_learning.py`

These can be safely deleted.

---

## 💡 Tips

1. **Run scripts from project root**: Scripts expect to be run from `/backend` directory
2. **Check jq installation**: Most scripts require `jq` for JSON parsing
3. **Wait for Keycloak**: Keycloak takes ~3-5 minutes to fully start after `docker-compose up`
4. **Use `-v` for verbose output**: Some scripts support verbose mode
5. **Check exit codes**: All scripts exit with non-zero on failure

---

## 📞 Need Help?

Check the logs:
```bash
# Service logs
docker-compose logs -f [service-name]

# Full simulation log
cat simulation_SUCCESS_FINAL.log
```
