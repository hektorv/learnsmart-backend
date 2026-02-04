#!/usr/bin/env python3
"""
Verify Deployment - Health Check
Checks the status of all microservices and the Gateway.
"""
import requests
import sys
import os

# Configuration
GATEWAY_URL = os.getenv("GATEWAY_URL", "http://localhost:8762")
SERVICES = [
    ("Gateway", f"{GATEWAY_URL}/actuator/health"),
    ("Eureka", os.getenv("EUREKA_URL", "http://localhost:8761").replace("/eureka/", "") + "/actuator/health"),
    # Access services via Gateway routes if possible, or direct if needed. 
    # For external verification, we check Gateway routes usually.
    # Note: Default actuator endpoints might not be exposed through Gateway for security.
    # We will check public endpoints or specific health checks.
]

def check_service(name, url, expected_codes=[200]):
    print(f"Checking {name} at {url}...", end=" ")
    try:
        response = requests.get(url, timeout=5)
        # Keycloak Realm check returns 200 with JSON realm info, but no "status" field.
        # So we just check status code.
        if response.status_code in expected_codes:
            print(f"✅ UP (HTTP {response.status_code})")
            return True
        else:
            print(f"⚠️  Status: HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def main():
    print("=== DEPLOYMENT HEALTH CHECK ===")
    print(f"Gateway: {GATEWAY_URL}\n")
    
    # 1. Check Core Infra
    # Gateway Actuator returns {"status": "UP"}
    gateway_up = False
    try:
        r = requests.get(f"{GATEWAY_URL}/actuator/health", timeout=5)
        if r.status_code == 200 and r.json().get("status") == "UP":
            print(f"Checking Gateway... ✅ UP")
            gateway_up = True
        else:
             print(f"Checking Gateway... ❌ DOWN ({r.status_code})")
    except:
        print(f"Checking Gateway... ❌ Unreachable")

    
    # 2. Check Keycloak (Public)
    keycloak_url = os.getenv("KEYCLOAK_URL", "http://localhost:8080")
    kc_up = check_service("Keycloak", f"{keycloak_url}/realms/master", [200])
    
    # 3. Check Microservices
    services_to_test = [
        ("Profile Service", "/profiles/me/health-check-public", [400, 401, 403, 404]), # 400/401/403 means auth filter hit service.
        ("Content Service", "/content/domains", [200, 400, 401]),
        ("Planning Service", "/planning/plans/health-check-public", [400, 401, 403, 404]),
    ]

    print("\n--- Service Reachability (via Gateway) ---")
    all_success = gateway_up and kc_up
    
    for name, path, expected in services_to_test:
        url = f"{GATEWAY_URL}{path}"
        if check_service(name, url, expected):
            pass
        else:
            all_success = False

    if all_success:
        print("\n✅ SYSTEM HEALTHY")
        sys.exit(0)
    else:
        print("\n❌ SYSTEM ISSUES DETECTED")
        sys.exit(1)

if __name__ == "__main__":
    main()
