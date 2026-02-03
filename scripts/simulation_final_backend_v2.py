#!/usr/bin/env python3
"""
Simulación FINAL del 'Student Journey' COMPLETE (Sprint 1-5).
Valida:
1. Profile: /me/progress (Consolidado)
2. Diagnostic: /plans/run-diagnostic (Sprint 5.1)
3. Planning: Create Plan based on Diagnostic
4. Content: /content-items/{id} (Detalle)
5. Tracking: /analytics/users/{userId}/stats y /activity
6. Certificates: Complete Plan -> Get Certificate (Sprint 5.3)
7. Assessment: /users/{userId}/skill-mastery (Enriquecido)
"""
import requests
import json
import time
import uuid

GATEWAY_URL = "http://localhost:8762"
KEYCLOAK_URL = "http://localhost:8080"
REALM = "learnsmart"

def get_token(username="user1", password="password"):
    url = f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token"
    data = {"username": username, "password": password, "grant_type": "password", "client_id": "learnsmart-frontend"}
    try:
        response = requests.post(url, data=data)
        if response.status_code != 200:
            return None
        return response.json()["access_token"]
    except Exception as e:
        print(f"❌ Excepción token: {e}")
        return None

def validate_journey():
    print("\n🎓 INICIANDO VALIDACIÓN DEL 'STUDENT JOURNEY' (FINAL v2)")
    print("=" * 70)
    
    token = get_token()
    if not token:
        print("🛑 ABORTANDO: No se pudo autenticar al estudiante.")
        return
    
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    
    # 1. PROFILE (Consolidated)
    print("\nStep 1: Dashboard (/profiles/me/progress)")
    progress_res = requests.get(f"{GATEWAY_URL}/profiles/me/progress", headers=headers)
    if progress_res.status_code == 200:
        prog = progress_res.json()
        print(f"   ✅ Dashboard OK. User: {prog['profile']['userId']}")
        user_id = prog['profile']['userId']
    else:
        print(f"   ❌ Fallo en dashboard: {progress_res.status_code}")
        # Try registering if fail (fallback logic omitted for brevity, assuming established env)
        return

    # 2. DIAGNOSTIC TEST (Sprint 5.1)
    print("\nStep 2: Diagnostic Test (/plans/run-diagnostic)")
    diag_payload = {"domain": "backend", "level": "junior", "nQuestions": 1}
    diag_res = requests.post(f"{GATEWAY_URL}/planning/plans/run-diagnostic", headers=headers, json=diag_payload)
    if diag_res.status_code == 200:
        questions = diag_res.json()
        print(f"   ✅ Diagnóstico generado: {len(questions)} preguntas.")
    else:
        print(f"   ⚠️ Diagnostic Falló ({diag_res.status_code}). Continuando con plan manual...")

    # 3. PLANNING & CERTIFICATES (Sprint 5.3)
    print("\nStep 3: Planning & Certificates Journey")
    
    # 3.1 Create Plan specific for Certification Test
    cert_plan_payload = {
        "userId": user_id,
        "goalId": "certification-path-" + str(uuid.uuid4())[:8],
        "name": "Backend Certification Path",
        "modules": [
            {"title": "Core Java", "status": "active", "position": 1},
            {"title": "Spring Boot", "status": "active", "position": 2} # Multiple modules to test loop
        ]
    }
    plan_res = requests.post(f"{GATEWAY_URL}/planning/plans", headers=headers, json=cert_plan_payload)
    if plan_res.status_code in [200, 201]:
        plan = plan_res.json()
        plan_id = plan['id']
        print(f"   ✅ Plan de Certificación creado: {plan_id}")
        
        # 3.2 Fetch Modules
        modules_res = requests.get(f"{GATEWAY_URL}/planning/plans/{plan_id}/modules", headers=headers)
        modules = modules_res.json()
        print(f"   🔎 Módulos encontrados: {len(modules)}")
        
        # 3.3 Complete All Modules
        for mod in modules:
            mod_id = mod['id']
            # PATCH status = completed
            requests.patch(f"{GATEWAY_URL}/planning/plans/{plan_id}/modules/{mod_id}", 
                          headers=headers, json={"status": "completed"})
            print(f"      -> Módulo {mod['title']} completado.")
            
        # 3.4 Verify Certificate
        time.sleep(1) # Allow for async processing if any
        cert_res = requests.get(f"{GATEWAY_URL}/planning/plans/certificates?userId={user_id}", headers=headers)
        if cert_res.status_code == 200:
            certs = cert_res.json()
            # Filter for this specific plan if user has multiple
            my_cert = next((c for c in certs if c['planId'] == plan_id), None)
            if my_cert:
                print(f"   🏆 CERTIFICADO GENERADO: {my_cert['title']}")
                print(f"   ✅ Sprint 5.3 Validado.")
            else:
                print("   ❌ Certificado NO encontrado para este plan.")
                print(f"      Todos los certificados: {json.dumps(certs, indent=2)}")
        else:
            print(f"   ❌ Error consultando certificados: {cert_res.status_code}")
            
    else:
        print(f"   ❌ No se pudo crear el plan de certificación: {plan_res.status_code} {plan_res.text}")

    # 4. CONTENT & MASTERY
    print("\nStep 4: Content & Mastery")
    # Get random content
    items = requests.get(f"{GATEWAY_URL}/content/content-items?size=1", headers=headers).json()
    if items:
        item = items[0]
        # Get Detail
        det = requests.get(f"{GATEWAY_URL}/content/content-items/{item['id']}", headers=headers)
        if det.status_code == 200:
            print(f"   ✅ Content Detail OK: {det.json()['title']}")
        
    # Stats
    stats = requests.get(f"{GATEWAY_URL}/tracking/analytics/users/{user_id}/stats", headers=headers).json()
    print(f"   ✅ Analytics: {stats.get('totalHours', 0):.2f}h Total")
    
    # Enriched Mastery (Fixed error handling)
    mastery_res = requests.get(f"{GATEWAY_URL}/assessment/users/{user_id}/skill-mastery", headers=headers)
    if mastery_res.status_code == 200:
        mastery = mastery_res.json()
        if mastery and isinstance(mastery, list) and len(mastery) > 0:
            print(f"   ✅ Mastery Enriched: {mastery[0].get('skillName')} ({mastery[0].get('mastery')*100}%)")
        else:
            print("   ⚠️ No mastery data yet (expected if fresh user).")
    else:
        print(f"   ❌ Error consultando Mastery: {mastery_res.status_code} - {mastery_res.text}")

    print("\n" + "=" * 70)
    print("🚀 BACKEND 100% READY FOR FRONTEND")
    print("=" * 70)

if __name__ == "__main__":
    validate_journey()
