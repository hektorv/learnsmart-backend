
import requests
import json
import time

GATEWAY_URL = "http://localhost:8762"
KEYCLOAK_URL = "http://localhost:8080"
REALM = "learnsmart"
CLIENT_ID = "learnsmart-frontend"
USERNAME = "student_1770092315"
PASSWORD = "password"

def get_token():
    url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
    payload = {
        "client_id": CLIENT_ID,
        "username": "admin1", 
        "password": "password",
        "grant_type": "password"
    }
    try:
        response = requests.post(url, data=payload)
        response.raise_for_status()
        return response.json()["access_token"]
    except Exception as e:
        print(f"Failed to get token: {e}")
        return None

def test_adaptivity():
    token = get_token()
    if not token:
        return

    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    # 1. Create a dummy Plan to get a planId
    print("\n--- 1. Creating Dummy Plan ---")
    plan_payload = {
        "userId": "e67e3328-912f-4884-98ae-ce5bdcd7907f", # Admin UUID or Random
        "name": "Adaptivity Test Plan",
        "goalId": "test-goal",
        "modules": [] # triggers AI generation but we assume it works
    }
    # Direct to planning-service for simplicity or gateway? Gateway is safer.
    # create_plan_url = f"{GATEWAY_URL}/planning/plans"
    create_plan_url = "http://localhost:8083/plans"
    
    try:
        resp = requests.post(create_plan_url, headers=headers, json=plan_payload)
        if resp.status_code not in [200, 201]:
             print(f"Failed to create plan: {resp.text}")
             return
        plan = resp.json()
        plan_id = plan["id"]
        print(f"✅ Plan Created: {plan_id}")
    except Exception as e:
        print(f"Exception creating plan: {e}")
        return

    # 2. Create Assessment Session linked to Plan
    print("\n--- 2. Creating Assessment Session ---")
    session_payload = {
        "userId": plan["userId"],
        "planId": plan_id,
        "type": "diagnostic",
        "status": "in_progress"
    }
    
    # Direct to assessment-service via gateway
    # create_session_url = f"{GATEWAY_URL}/assessment/assessments/sessions"
    create_session_url = "http://localhost:8084/assessments/sessions"
    try:
        resp = requests.post(create_session_url, headers=headers, json=session_payload)
        if resp.status_code not in [200, 201]:
            print(f"Failed to create session: {resp.text}")
            return
        session = resp.json()
        session_id = session["id"]
        print(f"✅ Session Created: {session_id} (PlanId: {session['planId']})")
    except Exception as e:
        print(f"Exception creating session: {e}")
        return

    # 3. Complete functions
    print("\n--- 3. Completing Session (Trigger Replan) ---")
    # update_status_url = f"{GATEWAY_URL}/assessment/assessments/sessions/{session_id}/status?status=completed"
    update_status_url = f"http://localhost:8084/assessments/sessions/{session_id}/status?status=completed"
    
    try:
        resp = requests.put(update_status_url, headers=headers)
        if resp.status_code == 200:
            print("✅ Session Completed successfully.")
            print("Check logs for 'Replan called' or 'Failed to notify'.")
        else:
            print(f"❌ Failed to complete session: {resp.text}")
            
    except Exception as e:
        print(f"Exception completing session: {e}")

if __name__ == "__main__":
    test_adaptivity()
