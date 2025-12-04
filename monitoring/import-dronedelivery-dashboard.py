#!/usr/bin/env python3
"""
Import Grafana Dashboard cho DroneDelivery Test Metrics
"""

import requests
import json
import time
from pathlib import Path

def import_dashboard(grafana_url="http://localhost:3000", admin_user="admin", admin_password="admin"):
    """Import dashboard vào Grafana"""
    
    print("📊 Importing DroneDelivery Test Metrics Dashboard...")
    
    # Đọc dashboard JSON
    dashboard_path = Path(__file__).parent / "dronedelivery-test-dashboard.json"
    
    if not dashboard_path.exists():
        print(f"❌ Dashboard file not found: {dashboard_path}")
        return False
    
    with open(dashboard_path, 'r') as f:
        dashboard = json.load(f)
    
    # Prepare import payload
    import_payload = {
        "dashboard": dashboard,
        "overwrite": True,
        "message": "Imported DroneDelivery Test Metrics Dashboard"
    }
    
    try:
        # Import dashboard
        response = requests.post(
            f"{grafana_url}/api/dashboards/db",
            json=import_payload,
            auth=(admin_user, admin_password),
            timeout=10
        )
        
        if response.status_code in [200, 201]:
            result = response.json()
            print(f"✅ Dashboard imported successfully!")
            print(f"   Dashboard URL: {grafana_url}/d/{result.get('uid', 'dronedelivery-tests')}")
            return True
        else:
            print(f"❌ Failed to import dashboard: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to Grafana at {grafana_url}")
        print("   Make sure Grafana is running: docker compose up -d")
        return False
    except Exception as e:
        print(f"❌ Error importing dashboard: {e}")
        return False

def wait_for_grafana(grafana_url="http://localhost:3000", timeout=60):
    """Đợi cho Grafana sẵn sàng"""
    print("⏳ Waiting for Grafana to be ready...")
    
    start_time = time.time()
    while time.time() - start_time < timeout:
        try:
            response = requests.get(f"{grafana_url}/api/health", timeout=5)
            if response.status_code == 200:
                print("✅ Grafana is ready!")
                return True
        except:
            pass
        
        time.sleep(2)
    
    print(f"⚠️ Grafana not ready after {timeout} seconds")
    return False

def main():
    grafana_url = "http://localhost:3000"
    
    # Wait for Grafana
    if not wait_for_grafana(grafana_url):
        print("Cannot proceed without Grafana")
        return 1
    
    # Import dashboard
    if import_dashboard(grafana_url):
        print("\n✨ Dashboard is ready!")
        print(f"   Open: {grafana_url}/d/dronedelivery-tests")
        return 0
    else:
        return 1

if __name__ == "__main__":
    import sys
    sys.exit(main())
