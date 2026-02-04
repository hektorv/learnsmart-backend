#!/usr/bin/env python3
"""
Simulate a Single Topic Plan to verify minimal flow.
"""
import requests
import json
import time
import os
import uuid

# Configuration
GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8762")
KEYCLOAK_URL = os.getenv("KEYCLOAK_URL", "http://localhost:8080")
REALM = "learnsmart"
ADMIN_USER = os.getenv("ADMIN_USERNAME", "admin1")
ADMIN_PASS = os.getenv("ADMIN_PASSWORD", "password")
CLIENT_ID = "learnsmart-frontend"

class LearnSmartClient:
    def __init__(self, role="USER"):
        self.token = None
        self.role = role
        self.user_id = None

    def login(self, username, password):
        url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
        data = { "grant_type": "password", "client_id": CLIENT_ID, "username": username, "password": password }
        try:
            response = requests.post(url, data=data)
            response.raise_for_status()
            self.token = response.json()["access_token"]
            print(f"[{self.role}] Login successful for {username}")
        except Exception as e:
            print(f"[{self.role}] Login failed: {e}")
            raise

    def get_headers(self):
        return { "Authorization": f"Bearer {self.token}", "Content-Type": "application/json" }

    def post(self, path, data):
        response = requests.post(f"{GATEWAY_URL}{path}", headers=self.get_headers(), json=data)
        return self._handle_response(response, "POST", path)

    def put(self, path, data):
        response = requests.put(f"{GATEWAY_URL}{path}", headers=self.get_headers(), json=data)
        return self._handle_response(response, "PUT", path)
    
    def patch(self, path, data):
        response = requests.patch(f"{GATEWAY_URL}{path}", headers=self.get_headers(), json=data)
        return self._handle_response(response, "PATCH", path)

    def get(self, path, params=None):
        response = requests.get(f"{GATEWAY_URL}{path}", headers=self.get_headers(), params=params)
        return self._handle_response(response, "GET", path)

    def _handle_response(self, response, method, path):
        if response.status_code in [200, 201, 204]:
            print(f"  ✓ {method} {path} -> {response.status_code}")
            if response.content:
                try:
                    return response.json()
                except:
                    return response.text
            return None
        print(f"  ❌ {method} {path} -> {response.status_code}")
        print(f"  Response: {response.text}")
        return None

def run_simulation():
    print("=== STARTING SINGLE TOPIC SIMULATION ===\n")

    # 1. Register User
    timestamp = int(time.time())
    username = f"single_{timestamp}"
    email = f"{username}@example.com"
    password = "password123"

    print(f"[System] Creating user {username}...")
    master_token = requests.post(f"{KEYCLOAK_URL}/realms/master/protocol/openid-connect/token", 
        data={"username": "admin", "password": "admin", "grant_type": "password", "client_id": "admin-cli"}).json()['access_token']
    
    kc_payload = {"username": username, "email": email, "enabled": True, "emailVerified": True, "firstName": "Single", "lastName": "Topic", "credentials": [{"type": "password", "value": password, "temporary": False}]}
    requests.post(f"{KEYCLOAK_URL}/admin/realms/{REALM}/users", json=kc_payload, headers={"Authorization": f"Bearer {master_token}"})
    time.sleep(1)

    student = LearnSmartClient("STUDENT")
    student.login(username, password)
    
    print(f"[Student] Registering Profile...")
    student.post("/profiles", {"email": email, "password": password, "displayName": "Single Topic Student"})

    # 2. Get User ID
    progress = student.get("/profiles/me/progress")
    student.user_id = progress['profile']['userId']
    print(f"  > User ID: {student.user_id}")

    # 3. Create SINGLE Module Plan
    print("\n--- CREATING SINGLE MODULE PLAN ---")
    plan_payload = {
        "userId": student.user_id,
        "goalId": "quick-learn",
        "name": "One Topic Plan",
        "modules": [
            {"title": "Just One Thing", "description": "Quick check", "estimatedHours": 1, "position": 1, "status": "pending"}
        ]
    }
    plan = student.post("/planning/plans", plan_payload)
    plan_id = plan['id']
    print(f"  > Plan Created: {plan_id}")

    # 4. Complete Module
    modules = student.get(f"/planning/plans/{plan_id}/modules")
    if len(modules) != 1:
        print(f"❌ ERROR: Expected 1 module, found {len(modules)}")
        return

    m = modules[0]
    print(f"  > Completing module: {m['title']}...")
    student.patch(f"/planning/plans/{plan_id}/modules/{m['id']}", {"status": "completed"})
    
    # Track View (for Analytics)
    student.post("/tracking/events", {
        "userId": student.user_id,
        "eventType": "content_view", 
        "entityId": m.get('contentId') or m['id'],
        "payload": json.dumps({"durationSeconds": 3600}) # 1 hour
    })

    # Complete Plan
    student.post("/tracking/events", {
        "userId": student.user_id, 
        "eventType": "PLAN_COMPLETED", 
        "entityId": plan_id,
        "payload": json.dumps({"score": 100})
    })

    # 5. Verify Analytics
    print("\n--- VERIFYING ANALYTICS ---")
    time.sleep(1) # Allow processing
    stats = student.get(f"/tracking/analytics/users/{student.user_id}/stats")
    
    actual_lessons = stats.get('lessonsCompleted', 0)
    actual_hours = stats.get('totalHours', 0.0)
    
    print(f"  > Stats: {actual_lessons} lessons, {actual_hours}h study.")
    
    if actual_lessons == 1 and actual_hours == 1.0:
        print("\n✅ SUCCESS: Single topic stats correct.")
    else:
        print(f"\n❌ FAILURE: Stats mismatch. Expected 1 lesson, 1.0h.")

if __name__ == "__main__":
    run_simulation()
