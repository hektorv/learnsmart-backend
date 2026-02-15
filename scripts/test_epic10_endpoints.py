#!/usr/bin/env python3
"""
Test script for Epic 10 US-10-08 and US-10-09 endpoints
Validates AI-driven assessment generation and skill auto-linking
"""

import requests
import json
import sys
from datetime import datetime

BASE_URL = "http://localhost:8762"
KEYCLOAK_URL = "http://localhost:8080"

def get_admin_token():
    """Get admin token from Keycloak"""
    print("🔑 Getting admin token...")
    response = requests.post(
        f"{KEYCLOAK_URL}/realms/learnsmart/protocol/openid-connect/token",
        data={
            "client_id": "learnsmart-client",
            "username": "admin1",
            "password": "password",
            "grant_type": "password"
        }
    )
    if response.status_code == 200:
        token = response.json()["access_token"]
        print(f"  ✓ Token obtained: {token[:20]}...")
        return token
    else:
        print(f"  ❌ Failed to get token: {response.status_code}")
        print(f"  Response: {response.text}")
        sys.exit(1)

def get_content_items(token):
    """Get list of content items"""
    print("\n📚 Getting content items...")
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{BASE_URL}/content/content-items", headers=headers)
    
    if response.status_code == 200:
        items = response.json()
        print(f"  ✓ Found {len(items)} content items")
        if items:
            return items[0]["id"]
        else:
            print("  ⚠️  No content items found, creating one...")
            return create_content_item(token)
    else:
        print(f"  ❌ Failed to get content items: {response.status_code}")
        print(f"  Response: {response.text}")
        return None

def create_content_item(token):
    """Create a test content item"""
    print("\n➕ Creating test content item...")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # First get a domain
    response = requests.get(f"{BASE_URL}/content/domains", headers=headers)
    if response.status_code != 200:
        print(f"  ❌ Failed to get domains: {response.status_code}")
        return None
    
    domains = response.json()
    if not domains:
        print("  ❌ No domains found")
        return None
    
    domain_id = domains[0]["id"]
    print(f"  Using domain: {domains[0]['name']} ({domain_id})")
    
    # Create content item
    data = {
        "domainId": domain_id,
        "type": "TEXT",
        "title": "Test Content for Epic 10",
        "description": "This is a test content item to validate US-10-08 and US-10-09. It covers React hooks including useState, useEffect, and custom hooks.",
        "estimatedMinutes": 30,
        "difficulty": "INTERMEDIATE",
        "active": True
    }
    
    response = requests.post(
        f"{BASE_URL}/content/content-items",
        headers=headers,
        json=data
    )
    
    if response.status_code == 201:
        content_id = response.json()["id"]
        print(f"  ✓ Created content item: {content_id}")
        return content_id
    else:
        print(f"  ❌ Failed to create content item: {response.status_code}")
        print(f"  Response: {response.text}")
        return None

def test_generate_assessments(token, content_id):
    """Test US-10-08: Generate Assessment Items"""
    print(f"\n🎯 Testing US-10-08: Generate Assessment Items")
    print(f"  Content ID: {content_id}")
    
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    data = {"nItems": 5}
    
    response = requests.post(
        f"{BASE_URL}/content/content-items/{content_id}/assessments/generate",
        headers=headers,
        json=data
    )
    
    print(f"  Status: {response.status_code}")
    
    if response.status_code == 200:
        result = response.json()
        print(f"  ✓ SUCCESS: Generated {len(result.get('items', []))} assessment items")
        
        # Display first item as example
        if result.get('items'):
            item = result['items'][0]
            print(f"\n  📝 Example Assessment Item:")
            print(f"     Question: {item.get('question', 'N/A')[:80]}...")
            print(f"     Options: {len(item.get('options', []))} choices")
            print(f"     Correct: Option {item.get('correctIndex', 'N/A')}")
            print(f"     Difficulty: {item.get('difficulty', 'N/A')}")
            print(f"     Explanation: {item.get('explanation', 'N/A')[:60]}...")
        
        return True
    else:
        print(f"  ❌ FAILED: {response.status_code}")
        print(f"  Response: {response.text}")
        return False

def test_auto_link_skills(token, content_id):
    """Test US-10-09: Auto-Link Skills"""
    print(f"\n🔗 Testing US-10-09: Auto-Link Skills")
    print(f"  Content ID: {content_id}")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    response = requests.post(
        f"{BASE_URL}/content/content-items/{content_id}/skills/auto-link",
        headers=headers
    )
    
    print(f"  Status: {response.status_code}")
    
    if response.status_code == 200:
        result = response.json()
        skill_codes = result.get('suggestedSkillCodes', [])
        print(f"  ✓ SUCCESS: Suggested {len(skill_codes)} skill codes")
        
        if skill_codes:
            print(f"\n  🏷️  Suggested Skills:")
            for code in skill_codes:
                print(f"     - {code}")
        
        return True
    else:
        print(f"  ❌ FAILED: {response.status_code}")
        print(f"  Response: {response.text}")
        return False

def main():
    print("=" * 60)
    print("Epic 10 US-10-08 & US-10-09 Endpoint Validation")
    print("=" * 60)
    
    # Get admin token
    token = get_admin_token()
    
    # Get or create content item
    content_id = get_content_items(token)
    if not content_id:
        print("\n❌ Cannot proceed without a content item")
        sys.exit(1)
    
    # Test US-10-08
    test1_passed = test_generate_assessments(token, content_id)
    
    # Test US-10-09
    test2_passed = test_auto_link_skills(token, content_id)
    
    # Summary
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"US-10-08 (Generate Assessments): {'✅ PASSED' if test1_passed else '❌ FAILED'}")
    print(f"US-10-09 (Auto-Link Skills):     {'✅ PASSED' if test2_passed else '❌ FAILED'}")
    print("=" * 60)
    
    if test1_passed and test2_passed:
        print("\n🎉 All Epic 10 endpoints are working correctly!")
        sys.exit(0)
    else:
        print("\n⚠️  Some endpoints failed validation")
        sys.exit(1)

if __name__ == "__main__":
    main()
