
import requests
import os
import json
import time

GATEWAY_URL = "http://localhost:8762"
KEYCLOAK_URL = "http://localhost:8080"
REALM = "learnsmart"
CLIENT_ID = "learnsmart-frontend"
USERNAME = "student_1770092315" # reusing existing user or creates new
PASSWORD = "password"

def get_token():
    url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
    payload = {
        "client_id": CLIENT_ID,
        "username": "admin1", # Use admin to be sure or any user
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

def test_diagnostic():
    token = get_token()
    if not token:
        print("Skipping test due to token failure.")
        return

    print("--- Testing Diagnostic Endpoint ---")
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    # Direct to planning service via gateway (port 8762 -> planning:8083)
    # Testing DIRECTLY first to isolate Gateway vs Service issue.
    # url = f"{GATEWAY_URL}/planning/plans/diagnostic"
    url = "http://localhost:8083/plans/run-diagnostic"
    payload = {
        "domain": "React",
        "level": "BEGINNER",
        "nQuestions": 2
    }
    
    try:
        print(f"POST {url}")
        resp = requests.post(url, headers=headers, json=payload)
        print(f"Status: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print("Response:")
            print(json.dumps(data, indent=2))
            if "questions" in data and len(data["questions"]) > 0:
                print("✅ Diagnostic Test Generated Successfully")
            else:
                print("⚠️ Valid response but no questions?")
        else:
            print(f"❌ Error: {resp.text}")
            
    except Exception as e:
        print(f"Exception: {e}")

if __name__ == "__main__":
    test_diagnostic()
