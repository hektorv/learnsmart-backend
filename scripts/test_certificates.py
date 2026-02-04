
import requests
import json
import uuid
import time

import os

GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8762")
KEYCLOAK_URL = os.getenv("KEYCLOAK_URL", "http://localhost:8080")
REALM = os.getenv("REALM", "learnsmart")
CLIENT_ID = "learnsmart-frontend"
ADMIN_USER = os.getenv("ADMIN_USERNAME", "admin1")
ADMIN_PASS = os.getenv("ADMIN_PASSWORD", "password")

def get_token():
    url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
    payload = {
        "client_id": CLIENT_ID,
        "username": ADMIN_USER, 
        "password": ADMIN_PASS,
        "grant_type": "password"
    }
    try:
        response = requests.post(url, data=payload)
        response.raise_for_status()
        return response.json()["access_token"]
    except Exception as e:
        print(f"Failed to get token: {e}")
        return None

def test_certificates():
    print("--- Testing Certificates ---")
    
    token = get_token()
    if not token:
        print("❌ Failed to get token. Exiting.")
        return

    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    # 1. Create Plan
    user_id = str(uuid.uuid4())
    print(f"User ID: {user_id}")
    
    plan_payload = {
        "userId": user_id,
        "name": "Cert Test Plan",
        "goalId": "cert-goal",
        "modules": [
            {"title": "Module 1", "description": "Intro", "status": "active"},
            {"title": "Module 2", "description": "Advanced", "status": "active"}
        ]
    }
    
    print("Creating Plan...")
    resp = requests.post(f"{GATEWAY_URL}/planning/plans", headers=headers, json=plan_payload)
    if resp.status_code != 201:
        print(f"Failed to create plan: {resp.status_code} {resp.text}")
        return
    
    plan = resp.json()
    plan_id = plan["id"]
    print(f"Plan Created: {plan_id}")
    
    modules = plan.get("modules", [])
    if not modules:
        # Refetch modules if not returned in create response (depending on implementation)
        resp = requests.get(f"{GATEWAY_URL}/planning/plans/{plan_id}/modules", headers=headers)
        modules = resp.json()
        
    print(f"Modules: {len(modules)}")
    
    # 2. Complete Modules
    for mod in modules:
        mod_id = mod["id"]
        print(f"Completing Module {mod_id}...")
        # Update status
        # PATCH /plans/{planId}/modules/{moduleId}
        update_payload = {"status": "completed"}
        resp = requests.patch(f"{GATEWAY_URL}/planning/plans/{plan_id}/modules/{mod_id}", headers=headers, json=update_payload)
        if resp.status_code != 200:
            print(f"Failed to update module {mod_id}: {resp.text}")
            return
    
    # 3. Check Certificate
    print("Checking Certificates...")
    # Give a moment for async if any (though it's sync in implementation)
    time.sleep(1)
    
    resp = requests.get(f"{GATEWAY_URL}/planning/plans/certificates?userId={user_id}", headers=headers)
    if resp.status_code == 200:
        certs = resp.json()
        print(f"Certificates found: {len(certs)}")
        if len(certs) > 0:
            c = certs[0]
            print(f"Certificate: {c['title']} - {c['description']}")
            print("✅ TEST PASSED: Certificate generated.")
        else:
            print("❌ TEST FAILED: No certificate found.")
    else:
        print(f"❌ TEST FAILED: Get certificates error: {resp.status_code} {resp.text}")

if __name__ == "__main__":
    test_certificates()
