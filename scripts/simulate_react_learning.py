#!/usr/bin/env python3
"""
Simulate React Learning Flow using the verified backend endpoints.
Matches the logic from 'simulation_final_backend_v2.py' (The Golden Test).

Validates:
1. Content Creation (Admin)
2. User Registration & Auth
3. Profile & Preferences
4. Diagnostic Test (Sprint 5.1)
5. Planning & Module Generation
6. Learning & Assessment
7. Certification (Sprint 5.3)
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
ADMIN_USER = "admin1"
ADMIN_PASS = "password"
CLIENT_ID = "learnsmart-frontend"

class LearnSmartClient:
    def __init__(self, role="USER"):
        self.token = None
        self.role = role
        self.user_id = None

    def login(self, username, password):
        url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
        data = {
            "grant_type": "password",
            "client_id": CLIENT_ID,
            "username": username,
            "password": password,
        }
        try:
            response = requests.post(url, data=data)
            response.raise_for_status()
            self.token = response.json()["access_token"]
            print(f"[{self.role}] Login successful for {username}")
        except Exception as e:
            print(f"[{self.role}] Login failed: {e}")
            raise

    def get_headers(self):
        return {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

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
    print("=== STARTING REACT LEARNING SIMULATION (v2) ===\n")

    # ==========================================
    # STEP 1: Content Setup (Admin)
    # ==========================================
    print("--- 1. CONTENT SETUP ---")
    admin = LearnSmartClient("ADMIN")
    admin.login(ADMIN_USER, ADMIN_PASS)

    # Check/Create Domain
    domains = admin.get("/content/domains", params={"code": "react-dev"})
    domain_list = domains if isinstance(domains, list) else domains.get('content', [])
    if domain_list:
        print(f"  > Domain found: {domain_list[0]['name']}")
    else:
        print("  > Creating 'react-dev' domain...")
        admin.post("/content/domains", {"code": "react-dev", "name": "React Development", "description": "Master React"})

    # Ensure Lesson Exists
    items = admin.get("/content/content-items", params={"size": 1})
    item_list = items if isinstance(items, list) else items.get('content', [])
    if not item_list:
        print("  > Creating Initial Content...")
        admin.post("/content/content-items", {
            "title": "React Hooks Intro",
            "type": "TEXT",
            "description": "Introduction to useState and useEffect",
            "content": "# React Hooks\nHooks let you use state...", 
            "status": "PUBLISHED"
        })

    # ==========================================
    # STEP 2: Student Registration
    # ==========================================
    print("\n--- 2. STUDENT REGISTRATION ---")
    timestamp = int(time.time())
    username = f"student_{timestamp}"
    email = f"{username}@example.com"
    password = "password123"

    # Register via Keycloak (Admin trick) to ensure clean state
    # (Simplified: assuming user exists or created via keycloak API directly)
    print(f"[System] Creating user {username}...")
    master_token = requests.post(f"{KEYCLOAK_URL}/realms/master/protocol/openid-connect/token", 
        data={"username": "admin", "password": "admin", "grant_type": "password", "client_id": "admin-cli"}).json()['access_token']
    
    kc_payload = {"username": username, "email": email, "enabled": True, "emailVerified": True, "firstName": "React", "lastName": "Student", "credentials": [{"type": "password", "value": password, "temporary": False}]}
    requests.post(f"{KEYCLOAK_URL}/admin/realms/{REALM}/users", json=kc_payload, headers={"Authorization": f"Bearer {master_token}"})
    time.sleep(1) # Propagate

    student = LearnSmartClient("STUDENT")
    student.login(username, password)
    
    # Register Profile
    print(f"[Student] Registering Profile...")
    student.post("/profiles", {"email": email, "password": password, "displayName": "React Student"})


    # ==========================================
    # STEP 3: Initial Profiling
    # ==========================================
    print("\n--- 3. INITIAL PROFILING ---")
    progress = student.get("/profiles/me/progress")
    student.user_id = progress['profile']['userId']
    print(f"  > User ID: {student.user_id}")

    # Set Preferences
    student.put("/profiles/me/preferences", {
        "hoursPerWeek": 12.0, 
        "preferredDays": ["SATURDAY", "SUNDAY"],
        "notificationsEnabled": True
    })

    # ==========================================
    # STEP 4: Diagnostic Test (Sprint 5.1)
    # ==========================================
    print("\n--- 4. DIAGNOSTIC TEST (Sprint 5.1) ---")
    diagnostic = student.post("/planning/plans/run-diagnostic", {
        "domain": "react-dev",
        "level": "JUNIOR",
        "nQuestions": 1
    })
    print(f"  > Generated {len(diagnostic)} diagnostic questions.")


    # ==========================================
    # STEP 5: Planning & Modules
    # ==========================================
    print("\n--- 5. PLAN CREATION ---")
    # Create Certification Plan
    plan_payload = {
        "userId": student.user_id,
        "goalId": "react-cert",
        "name": "React Developer Certification",
        "modules": [
            {"title": "React Fundamentals", "description": "Components & Props", "estimatedHours": 5, "position": 1, "status": "pending"},
            {"title": "Advanced Hooks", "description": "Custom Hooks & Performance", "estimatedHours": 8, "position": 2, "status": "pending"}
        ]
    }
    plan = student.post("/planning/plans", plan_payload)
    plan_id = plan['id']
    print(f"  > Plan Created: {plan_id}")

    # Verify Modules
    modules = student.get(f"/planning/plans/{plan_id}/modules")
    print(f"  > Modules found: {len(modules)}")
    for m in modules:
        print(f"    - {m['title']} ({m['status']})")


    # ==========================================
    # STEP 6: Learning & Completion
    # ==========================================
    print("\n--- 6. LEARNING & COMPLETION ---")
    
    # Complete Modules
    for m in modules:
        print(f"  > Completing module: {m['title']}...")
        student.patch(f"/planning/plans/{plan_id}/modules/{m['id']}", {"status": "completed"})

    # Record Activity
    student.post("/tracking/events", {
        "userId": student.user_id, 
        "eventType": "PLAN_COMPLETED", 
        "entityId": plan_id,
        "payload": json.dumps({"score": 100})
    })


    # ==========================================
    # STEP 7: Certification (Sprint 5.3)
    # ==========================================
    print("\n--- 7. CERTIFICATION (Sprint 5.3) ---")
    time.sleep(2) # Allow async processing
    certs = student.get(f"/planning/plans/certificates?userId={student.user_id}")
    
    if certs:
        my_cert = next((c for c in certs if c['planId'] == plan_id), None)
        if my_cert:
            print(f"  🏆 CERTIFICATE EARNED: {my_cert['title']}")
            print(f"     Issued At: {my_cert['issuedAt']}")
        else:
            print("  ⚠️ Certificate not found for this plan.")
    else:
        print("  ⚠️ No certificates found.")


    # ==========================================
    # STEP 8: Analytics & Mastery
    # ==========================================
    print("\n--- 8. ANALYTICS & MASTERY ---")
    stats = student.get(f"/tracking/analytics/users/{student.user_id}/stats")
    print(f"  > Stats: {stats.get('lessonsCompleted', 0)} lessons, {stats.get('totalHours', 0)}h study.")

    mastery = student.get(f"/assessment/users/{student.user_id}/skill-mastery")
    if isinstance(mastery, list):
        print(f"  > Mastery records: {len(mastery)}")
    else:
        print(f"  > Mastery check failed or empty.")

    print("\n=== SIMULATION COMPLETED SUCCESSFULLY ===")

if __name__ == "__main__":
    run_simulation()
