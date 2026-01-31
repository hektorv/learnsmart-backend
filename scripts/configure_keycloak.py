import requests
import json
import time

KEYCLOAK_URL = "http://localhost:8080"
ADMIN_USER = "admin"
ADMIN_PASSWORD = "admin"
REALM_NAME = "learnsmart"

def get_admin_token():
    url = f"{KEYCLOAK_URL}/realms/master/protocol/openid-connect/token"
    payload = {
        "client_id": "admin-cli",
        "username": ADMIN_USER,
        "password": ADMIN_PASSWORD,
        "grant_type": "password"
    }
    response = requests.post(url, data=payload)
    response.raise_for_status()
    return response.json()["access_token"]

def create_realm(token):
    url = f"{KEYCLOAK_URL}/admin/realms"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    payload = {
        "realm": REALM_NAME,
        "enabled": True,
        "displayName": "LearnSmart Platform"
    }
    response = requests.post(url, headers=headers, json=payload)
    if response.status_code == 201:
        print(f"Realm '{REALM_NAME}' created successfully.")
    elif response.status_code == 409:
        print(f"Realm '{REALM_NAME}' already exists.")
    else:
        print(f"Failed to create realm: {response.text}")
        response.raise_for_status()

def create_client(token, client_data):
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/clients"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    response = requests.post(url, headers=headers, json=client_data)
    if response.status_code == 201:
        print(f"Client '{client_data['clientId']}' created successfully.")
        return get_client_id(token, client_data['clientId'])
    elif response.status_code == 409:
        print(f"Client '{client_data['clientId']}' already exists.")
        return get_client_id(token, client_data['clientId'])
    else:
        print(f"Failed to create client: {response.text}")
        response.raise_for_status()

def get_client_id(token, client_id):
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/clients?clientId={client_id}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    clients = response.json()
    if clients:
        return clients[0]['id']
    return None

def get_client_secret(token, client_uuid):
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/clients/{client_uuid}/client-secret"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()['value']

def create_role(token, role_name):
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/roles"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    payload = {"name": role_name}
    response = requests.post(url, headers=headers, json=payload)
    if response.status_code == 201:
        print(f"Role '{role_name}' created successfully.")
    elif response.status_code == 409:
        print(f"Role '{role_name}' already exists.")
    else:
        print(f"Failed to create role: {response.text}")
        response.raise_for_status()

def create_user(token, username, password, roles):
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/users"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    payload = {
        "username": username,
        "enabled": True,
        "email": f"{username}@example.com",
        "firstName": username.capitalize(),
        "lastName": "User",
        "credentials": [
            {
                "type": "password",
                "value": password,
                "temporary": False
            }
        ]
    }
    response = requests.post(url, headers=headers, json=payload)
    if response.status_code == 201:
        print(f"User '{username}' created successfully.")
        user_id = get_user_id(token, username)
        assign_roles(token, user_id, roles)
    elif response.status_code == 409:
        print(f"User '{username}' already exists.")
    else:
        print(f"Failed to create user: {response.text}")
        response.raise_for_status()

def get_user_id(token, username):
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/users?username={username}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    users = response.json()
    if users:
        return users[0]['id']
    return None

def assign_roles(token, user_id, role_names):
    # Get role representations
    role_reps = []
    for role_name in role_names:
        url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/roles/{role_name}"
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            role_reps.append(response.json())
    
    # Assign
    url = f"{KEYCLOAK_URL}/admin/realms/{REALM_NAME}/users/{user_id}/role-mappings/realm"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    requests.post(url, headers=headers, json=role_reps)
    print(f"Roles {role_names} assigned to user.")

def main():
    try:
        print("Waiting for Keycloak to be ready...")
        time.sleep(5)
        token = get_admin_token()
        print("Obtained admin token.")

        create_realm(token)
        
        # Public Client (Frontend)
        create_client(token, {
            "clientId": "learnsmart-frontend",
            "publicClient": True,
            "directAccessGrantsEnabled": True,
            "redirectUris": ["http://localhost:3000/*", "http://localhost/*"],
            "webOrigins": ["*"]
        })

        # Confidential Client (Gateway)
        gateway_uuid = create_client(token, {
            "clientId": "api-gateway",
            "publicClient": False,
            "serviceAccountsEnabled": True, # For client_credentials grant
            "standardFlowEnabled": True,
            "directAccessGrantsEnabled": True,
            "redirectUris": ["http://localhost:8762/login/oauth2/code/keycloak"],
            "authorizationServicesEnabled": True
        })
        
        secret = get_client_secret(token, gateway_uuid)
        print(f"api-gateway client secret: {secret}")
        
        create_role(token, "USER")
        create_role(token, "ADMIN")
        
        create_user(token, "user1", "password", ["USER"])
        create_user(token, "admin1", "password", ["ADMIN"])
        
        print("\nKeycloak configuration completed successfully!")
        print(f"Client Secret for 'api-gateway': {secret}")
        
        # Save secret to a file for later use
        with open("client_secret.txt", "w") as f:
            f.write(secret)

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
