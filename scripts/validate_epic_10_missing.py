#!/usr/bin/env python3
import requests
import json
import os
import time

# Configuration
GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8762")
KEYCLOAK_URL = os.getenv("KEYCLOAK_URL", "http://localhost:8080")
REALM = os.getenv("REALM", "learnsmart")
ADMIN_USER = os.getenv("ADMIN_USERNAME", "admin1")
ADMIN_PASS = os.getenv("ADMIN_PASSWORD", "password") # Standard password from other scripts
CLIENT_ID = "learnsmart-frontend"

def get_admin_token():
    url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
    data = {
        "grant_type": "password",
        "client_id": CLIENT_ID,
        "username": ADMIN_USER,
        "password": ADMIN_PASS,
    }
    try:
        response = requests.post(url, data=data)
        response.raise_for_status()
        return response.json()["access_token"]
    except Exception as e:
        print(f"Login failed: {e}")
        return None

def main():
    print("=== VALIDATING EPIC 10 MISSING CRITERIA (US-10-06, US-10-07) ===")
    token = get_admin_token()
    if not token:
        return

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # 1. Get Domain
    print("\n1. Getting 'React Development' domain...")
    resp = requests.get(f"{GATEWAY_URL}/content/domains?code=react-dev", headers=headers)
    if resp.status_code != 200:
        print(f"❌ Failed to get domain: {resp.status_code}")
        return
    
    domains = resp.json()
    domain_list = domains if isinstance(domains, list) else domains.get('content', [])
    if not domain_list:
        print("❌ Domain 'react-dev' not found. Please run core simulation first.")
        # Try to create it if missing
        print("  > Creating 'react-dev' domain...")
        create_resp = requests.post(f"{GATEWAY_URL}/content/domains", 
                                  json={"code": "react-dev", "name": "React Development", "description": "Master React"},
                                  headers=headers)
        if create_resp.status_code in [200, 201]:
           domain_id = create_resp.json()['id']
        else: 
           print(f"❌ Failed to create domain: {create_resp.status_code}")
           return
    else:
        domain_id = domain_list[0]['id']
    
    print(f"✅ Domain ID: {domain_id}")

    # 2. Test US-10-06: Generate Skills
    print("\n2. Testing US-10-06: AI Skill Discovery (POST /skills/domains/{id}/generate)...")
    gen_url = f"{GATEWAY_URL}/content/skills/domains/{domain_id}/generate"
    gen_payload = {"topic": "Redux Toolkit"}
    
    start_time = time.time()
    try:
        gen_resp = requests.post(gen_url, json=gen_payload, headers=headers)
        duration = time.time() - start_time
        
        if gen_resp.status_code in [200, 201]:
            skills = gen_resp.json()
            print(f"✅ US-10-06 PASSED: Generated {len(skills)} skills in {duration:.2f}s")
            for s in skills:
                print(f"   - {s.get('name')} ({s.get('code')})")
        else:
            print(f"❌ US-10-06 FAILED: {gen_resp.status_code}")
            print(gen_resp.text)
    except Exception as e:
         print(f"❌ US-10-06 EXCEPTION: {e}")

    # 3. Test US-10-07: Link Skills
    print("\n3. Testing US-10-07: AI Prerequisite Linking (POST /skills/domains/{id}/link)...")
    link_url = f"{GATEWAY_URL}/content/skills/domains/{domain_id}/link"
    
    try:
        link_resp = requests.post(link_url, headers=headers)
        
        if link_resp.status_code in [200, 204]:
            print(f"✅ US-10-07 PASSED: Skill linking triggered successfully ({link_resp.status_code})")
        else:
            print(f"❌ US-10-07 FAILED: {link_resp.status_code}")
            print(link_resp.text)
            
    except Exception as e:
         print(f"❌ US-10-07 EXCEPTION: {e}")

if __name__ == "__main__":
    main()
