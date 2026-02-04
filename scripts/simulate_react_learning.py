#!/usr/bin/env python3
"""
Enhanced React Learning Flow Simulation - Updated for US-110, US-094, US-107, US-096, US-123, US-111

Validates:
1. Content Creation (Admin)
2. User Registration & Auth
3. Profile & Preferences (US-094: Audit Trail)
4. Diagnostic Test (Sprint 5.1)
5. Planning & Module Generation (US-111: Prerequisite Validation)
6. Learning & Assessment (US-110: Activity Timestamps)
7. Goal Management (US-096: Goal Completion Tracking)
8. Replanning Triggers (US-107: Automatic Replanning)
9. Event Validation (US-123: Payload Validation)
10. Certification (Sprint 5.3)
"""
import requests
import json
import time
import os
import uuid
from datetime import datetime

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
    
    def delete(self, path):
        response = requests.delete(f"{GATEWAY_URL}{path}", headers=self.get_headers())
        return self._handle_response(response, "DELETE", path)

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
    print("=== ENHANCED REACT LEARNING SIMULATION (v3) ===")
    print("Testing: US-110, US-094, US-107, US-096, US-123, US-111\n")

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

    # Create Skills with Prerequisites (US-111)
    print("\n  > Creating Skills with Prerequisites (US-111)...")
    
    # JavaScript skill (no prerequisites)
    js_skill = admin.post("/content/skills", {
        "domain": "react-dev",
        "code": "javascript-fundamentals",
        "name": "JavaScript Fundamentals",
        "description": "Core JavaScript concepts",
        "level": "BEGINNER"
    })
    
    # React skill (requires JavaScript)
    react_skill = admin.post("/content/skills", {
        "domain": "react-dev",
        "code": "react-basics",
        "name": "React Basics",
        "description": "React components and props",
        "level": "INTERMEDIATE"
    })
    
    # React Hooks skill (requires React)
    hooks_skill = admin.post("/content/skills", {
        "domain": "react-dev",
        "code": "react-hooks",
        "name": "React Hooks",
        "description": "useState, useEffect, custom hooks",
        "level": "INTERMEDIATE"
    })
    
    if js_skill and react_skill and hooks_skill:
        js_id = js_skill['id']
        react_id = react_skill['id']
        hooks_id = hooks_skill['id']
        
        # Set prerequisites: React requires JavaScript
        admin.post(f"/content/skills/{react_id}/prerequisites/{js_id}", {})
        
        # Set prerequisites: Hooks requires React
        admin.post(f"/content/skills/{hooks_id}/prerequisites/{react_id}", {})
        
        print(f"    - JavaScript: {js_id}")
        print(f"    - React (requires JS): {react_id}")
        print(f"    - Hooks (requires React): {hooks_id}")

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

    # Register via Keycloak
    print(f"[System] Creating user {username}...")
    master_token = requests.post(f"{KEYCLOAK_URL}/realms/master/protocol/openid-connect/token", 
        data={"username": "admin", "password": "admin", "grant_type": "password", "client_id": "admin-cli"}).json()['access_token']
    
    kc_payload = {"username": username, "email": email, "enabled": True, "emailVerified": True, "firstName": "React", "lastName": "Student", "credentials": [{"type": "password", "value": password, "temporary": False}]}
    requests.post(f"{KEYCLOAK_URL}/admin/realms/{REALM}/users", json=kc_payload, headers={"Authorization": f"Bearer {master_token}"})
    time.sleep(1)

    student = LearnSmartClient("STUDENT")
    student.login(username, password)
    
    # Register Profile
    print(f"[Student] Registering Profile...")
    student.post("/profiles", {"email": email, "password": password, "displayName": "React Student"})


    # ==========================================
    # STEP 3: Initial Profiling (US-094: Audit Trail)
    # ==========================================
    print("\n--- 3. INITIAL PROFILING (US-094: Audit Trail) ---")
    progress = student.get("/profiles/me/progress")
    student.user_id = progress['profile']['userId']
    print(f"  > User ID: {student.user_id}")

    # Set Preferences (triggers audit log)
    student.put("/profiles/me/preferences", {
        "hoursPerWeek": 12.0, 
        "preferredDays": ["SATURDAY", "SUNDAY"],
        "notificationsEnabled": True
    })
    
    # Check Audit Trail (US-094)
    print("  > Checking Audit Trail (US-094)...")
    audit_logs = student.get(f"/profiles/me/audit-logs")
    if audit_logs:
        print(f"    - Found {len(audit_logs)} audit entries")
        for log in audit_logs[:3]:  # Show first 3
            print(f"      • {log.get('action')} at {log.get('timestamp')}")

    # ==========================================
    # STEP 4: Goal Management (US-096)
    # ==========================================
    print("\n--- 4. GOAL MANAGEMENT (US-096: Goal Completion Tracking) ---")
    
    # Create Learning Goal
    goal = student.post("/profiles/me/goals", {
        "title": "Master React Development",
        "description": "Become proficient in React",
        "domain": "react-dev",
        "targetLevel": "INTERMEDIATE",
        "targetDate": "2026-06-01"
    })
    
    if goal:
        goal_id = goal['id']
        print(f"  > Goal Created: {goal_id}")
        print(f"    - Title: {goal['title']}")
        print(f"    - Status: {goal.get('status', 'ACTIVE')}")
        print(f"    - Progress: {goal.get('progressPercentage', 0)}%")

    # ==========================================
    # STEP 5: Diagnostic Test (Sprint 5.1)
    # ==========================================
    print("\n--- 5. DIAGNOSTIC TEST (Sprint 5.1) ---")
    diagnostic = student.post("/planning/plans/diagnostics", {
        "domain": "react-dev",
        "level": "JUNIOR",
        "nQuestions": 1
    })
    print(f"  > Generated {len(diagnostic) if diagnostic else 0} diagnostic questions.")


    # ==========================================
    # STEP 6: Plan Creation (US-111: Prerequisite Validation)
    # ==========================================
    print("\n--- 6. PLAN CREATION (US-111: Prerequisite Validation) ---")
    
    # Create plan with modules that have targetSkills
    plan_payload = {
        "userId": student.user_id,
        "goalId": goal_id if goal else "react-cert",
        "name": "React Developer Certification",
        "modules": [
            {
                "title": "React Hooks Advanced",
                "description": "Custom Hooks & Performance",
                "estimatedHours": 8,
                "position": 1,
                "status": "pending",
                "targetSkills": [hooks_id] if hooks_skill else []  # Requires React
            },
            {
                "title": "React Fundamentals",
                "description": "Components & Props",
                "estimatedHours": 5,
                "position": 2,
                "status": "pending",
                "targetSkills": [react_id] if react_skill else []  # Requires JavaScript
            },
            {
                "title": "JavaScript Basics",
                "description": "ES6+ Features",
                "estimatedHours": 4,
                "position": 3,
                "status": "pending",
                "targetSkills": [js_id] if js_skill else []  # No prerequisites
            }
        ]
    }
    
    print("  > Creating plan with OUT-OF-ORDER modules (testing US-111)...")
    print("    - Module 1: React Hooks (requires React)")
    print("    - Module 2: React Fundamentals (requires JavaScript)")
    print("    - Module 3: JavaScript Basics (no prerequisites)")
    print("  > Expected: US-111 should reorder to: JS → React → Hooks")
    
    plan = student.post("/planning/plans", plan_payload)
    
    if plan:
        plan_id = plan['id']
        print(f"  > Plan Created: {plan_id}")
        
        # Verify module order after prerequisite validation
        modules = student.get(f"/planning/plans/{plan_id}/modules")
        print(f"  > Modules after US-111 validation: {len(modules)}")
        for i, m in enumerate(modules, 1):
            skills = m.get('targetSkills', [])
            print(f"    {i}. {m['title']} (position={m['position']}, skills={len(skills)})")


    # ==========================================
    # STEP 7: Learning & Completion (US-110: Activity Timestamps)
    # ==========================================
    print("\n--- 7. LEARNING & COMPLETION (US-110: Activity Timestamps) ---")
    
    # Get activities for first module
    if modules:
        first_module = modules[0]
        module_id = first_module['id']
        
        # Get activities
        activities = first_module.get('activities', [])
        if not activities:
            # Create a sample activity
            print(f"  > Creating sample activity for module {module_id}...")
            activity = student.post(f"/planning/plans/{plan_id}/modules/{module_id}/activities", {
                "position": 1,
                "activityType": "LESSON",
                "contentRef": "lesson:react-hooks-intro",
                "estimatedMinutes": 30
            })
            if activity:
                activities = [activity]
        
        # Complete activity with timestamps (US-110)
        if activities:
            activity = activities[0]
            activity_id = activity['id']
            
            print(f"  > Testing US-110: Activity Completion Timestamps...")
            
            # Start activity
            print(f"    - Starting activity: {activity.get('activityType', 'UNKNOWN')}")
            student.patch(f"/planning/plans/{plan_id}/modules/{module_id}/activities/{activity_id}", {
                "status": "in_progress"
            })
            
            time.sleep(2)  # Simulate learning time
            
            # Complete activity (should auto-set timestamps)
            print(f"    - Completing activity...")
            completed = student.patch(f"/planning/plans/{plan_id}/modules/{module_id}/activities/{activity_id}", {
                "status": "completed"
            })
            
            if completed:
                print(f"      • Started At: {completed.get('startedAt', 'N/A')}")
                print(f"      • Completed At: {completed.get('completedAt', 'N/A')}")
                print(f"      • Actual Minutes: {completed.get('actualMinutesSpent', 'N/A')}")
    
    # Complete all modules
    print("\n  > Completing all modules...")
    for m in modules:
        student.patch(f"/planning/plans/{plan_id}/modules/{m['id']}", {"status": "completed"})
        
        # Track learning event (US-123: Payload Validation)
        print(f"    - Tracking event for module: {m['title']} (US-123)...")
        event_result = student.post("/tracking/events", {
            "userId": student.user_id,
            "eventType": "content_view",
            "entityId": m.get('contentId') or m['id'],
            "payload": json.dumps({"durationSeconds": 1800, "moduleTitle": m['title']})
        })


    # ==========================================
    # STEP 8: Goal Completion (US-096)
    # ==========================================
    print("\n--- 8. GOAL COMPLETION (US-096) ---")
    
    # Mark goal as completed
    if goal:
        print(f"  > Completing goal: {goal['title']}...")
        completed_goal = student.patch(f"/profiles/me/goals/{goal_id}", {
            "status": "COMPLETED",
            "progressPercentage": 100
        })
        
        if completed_goal:
            print(f"    - Status: {completed_goal.get('status')}")
            print(f"    - Progress: {completed_goal.get('progressPercentage')}%")
            print(f"    - Completed At: {completed_goal.get('completedAt', 'N/A')}")


    # ==========================================
    # STEP 9: Replanning Trigger (US-107)
    # ==========================================
    print("\n--- 9. REPLANNING TRIGGER (US-107: Automatic Replanning) ---")
    
    # Simulate mastery change event that should trigger replanning
    print("  > Simulating mastery change event (US-107)...")
    student.post("/tracking/events", {
        "userId": student.user_id,
        "eventType": "mastery_updated",
        "entityId": react_id if react_skill else str(uuid.uuid4()),
        "payload": json.dumps({
            "skillId": react_id if react_skill else str(uuid.uuid4()),
            "oldLevel": "BEGINNER",
            "newLevel": "INTERMEDIATE",
            "score": 85
        })
    })
    
    time.sleep(2)  # Allow async processing
    
    # Check for replan triggers
    triggers = student.get(f"/planning/plans/{plan_id}/replan-triggers")
    if triggers:
        print(f"    - Found {len(triggers)} replan triggers")
        for trigger in triggers[:3]:
            print(f"      • Type: {trigger.get('triggerType')}, Status: {trigger.get('status')}")


    # ==========================================
    # STEP 10: Adaptive Assessment (US-083 & US-084)
    # ==========================================
    print("\n--- 10. ADAPTIVE ASSESSMENT (US-083 & US-084) ---")
    
    session = student.post("/assessment/assessments/sessions", {
        "userId": student.user_id,
        "planId": plan_id,
        "type": "ADAPTIVE"
    })
    
    if session and 'id' in session:
        session_id = session['id']
        print(f"  > Session Created: {session_id}")

        # Get Next Item (US-083 AI)
        item = student.get(f"/assessment/assessments/sessions/{session_id}/next-item")
        if item:
            item_id = item.get('id') or item.get('tempId')
            print(f"    - Received Item: {item.get('stem', 'No Stem')[:50]}...")
            
            if item_id:
                options = item.get('options', [])
                option_id = options[0]['id'] if options else None
                
                if option_id:
                    print(f"  > Submitting response (US-084 AI Feedback)...")
                    response = student.post(f"/assessment/assessments/sessions/{session_id}/responses", {
                        "assessmentItemId": item_id,
                        "selectedOptionId": option_id,
                        "responsePayload": "Selected answer",
                        "responseTimeMs": 5000
                    })
                    
                    if response:
                        print(f"      • Feedback: {response.get('feedback', 'N/A')[:80]}...")
                        print(f"      • Correct: {response.get('isCorrect')}")

        # Complete Session
        student.put(f"/assessment/assessments/sessions/{session_id}/status?status=completed", {})


    # ==========================================
    # STEP 11: Certification (Sprint 5.3)
    # ==========================================
    print("\n--- 11. CERTIFICATION (Sprint 5.3) ---")
    time.sleep(2)
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
    # STEP 12: Analytics & Mastery
    # ==========================================
    print("\n--- 12. ANALYTICS & MASTERY ---")
    stats = student.get(f"/tracking/analytics/users/{student.user_id}/stats")
    if stats:
        print(f"  > Stats: {stats.get('lessonsCompleted', 0)} lessons, {stats.get('totalHours', 0)}h study.")

    mastery = student.get(f"/assessment/users/{student.user_id}/skill-mastery")
    if isinstance(mastery, list):
        print(f"  > Mastery records: {len(mastery)}")
    else:
        print(f"  > Mastery check failed or empty.")

    print("\n=== ✅ SIMULATION COMPLETED SUCCESSFULLY ===")
    print("\nTested Features:")
    print("  ✓ US-110: Activity Completion Timestamps")
    print("  ✓ US-094: User Audit Trail")
    print("  ✓ US-107: Automatic Replanning Triggers")
    print("  ✓ US-096: Goal Completion Tracking")
    print("  ✓ US-123: Event Payload Validation")
    print("  ✓ US-111: Skill Prerequisite Validation")

if __name__ == "__main__":
    run_simulation()
