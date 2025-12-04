#!/usr/bin/env python3
"""
Update Grafana dashboard with DroneDelivery test metrics
"""

import requests
import json
import time
from pathlib import Path

def update_dashboard(grafana_url="http://localhost:3001", admin_user="admin", admin_password="admin"):
    """Update dashboard trong Grafana"""
    
    print("📊 Updating CNPM Dashboard with DroneDelivery Test Metrics...")
    
    # Đọc dashboard JSON
    dashboard_path = Path(__file__).parent / "grafana-dashboard.json"
    
    if not dashboard_path.exists():
        print(f"❌ Dashboard file not found: {dashboard_path}")
        return False
    
    with open(dashboard_path, 'r', encoding='utf-8') as f:
        dashboard = json.load(f)
    
    # Prepare update payload
    update_payload = {
        "dashboard": dashboard,
        "overwrite": True,
        "message": "Updated with DroneDelivery Test Metrics"
    }
    
    try:
        # Update dashboard
        response = requests.post(
            f"{grafana_url}/api/dashboards/db",
            json=update_payload,
            auth=(admin_user, admin_password),
            timeout=10
        )
        
        if response.status_code in [200, 201]:
            result = response.json()
            print(f"✅ Dashboard updated successfully!")
            print(f"   Dashboard URL: {grafana_url}/d/{result.get('uid', 'cnpm-system-dashboard')}")
            return True
        else:
            print(f"❌ Failed to update dashboard: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to Grafana at {grafana_url}")
        print("   Make sure Grafana is running: docker compose up -d")
        return False
    except Exception as e:
        print(f"❌ Error updating dashboard: {e}")
        return False

def main():
    grafana_url = "http://localhost:3001"
    
    # Update dashboard
    if update_dashboard(grafana_url, admin_user="admin", admin_password="1admin1"):
        print("\n✨ Dashboard updated successfully!")
        print(f"   Open: {grafana_url}/d/cnpm-system-dashboard")
        return 0
    else:
        return 1

if __name__ == "__main__":
    import sys
    sys.exit(main())
