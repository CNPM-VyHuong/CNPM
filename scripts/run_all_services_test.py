#!/usr/bin/env python3
"""
Run tests for all services sequentially and update dashboard
Shows results for each service one by one with real-time dashboard updates
"""

import os
import sys
import subprocess
from pathlib import Path
from datetime import datetime

class AllServicesTestRunner:
    def __init__(self, backend_path: str):
        self.backend_path = Path(backend_path)
        self.services = [
            'user_service',
            'product_service',
            'drone_service',
            'order_service',
            'payment_service',
            'restaurant-service'
        ]
        self.script_path = self.backend_path.parent / "scripts" / "run_single_service_test.py"
        self.results = {}
    
    def run_all_services(self) -> int:
        """Run tests for all services sequentially"""
        print(f"\n{'='*70}")
        print(f"[*] RUNNING TESTS FOR ALL SERVICES")
        print(f"[*] Start Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"{'='*70}\n")
        
        total_failed = 0
        
        for i, service in enumerate(self.services, 1):
            print(f"\n{'='*70}")
            print(f"[*] SERVICE {i}/{len(self.services)}: {service.upper()}")
            print(f"{'='*70}\n")
            
            cmd = [
                "python",
                str(self.script_path),
                str(self.backend_path),
                service
            ]
            
            try:
                result = subprocess.run(cmd, cwd=str(self.backend_path.parent))
                
                if result.returncode == 0:
                    self.results[service] = "✓ PASS"
                    print(f"[✓] {service} - PASSED")
                else:
                    self.results[service] = "✗ FAIL"
                    total_failed += 1
                    print(f"[✗] {service} - FAILED")
                    
            except Exception as e:
                self.results[service] = f"✗ ERROR: {e}"
                total_failed += 1
                print(f"[!] Error running {service}: {e}")
            
            # Add delay between services
            print(f"\n[*] Waiting 5 seconds before next service...\n")
            import time
            time.sleep(5)
        
        self.print_summary()
        return total_failed
    
    def print_summary(self):
        """Print summary of all test results"""
        print(f"\n{'='*70}")
        print(f"[*] SUMMARY - ALL SERVICES TEST RUN")
        print(f"{'='*70}\n")
        
        passed_count = sum(1 for v in self.results.values() if "PASS" in v)
        failed_count = sum(1 for v in self.results.values() if "FAIL" in v)
        
        for service, result in self.results.items():
            print(f"{result:8} | {service}")
        
        print(f"\n{'='*70}")
        print(f"Total Services: {len(self.results)}")
        print(f"Passed:         {passed_count}")
        print(f"Failed:         {failed_count}")
        print(f"{'='*70}\n")
        
        print("[✓] All results updated in Grafana")
        print("[*] Dashboard URL: http://localhost:3001")
        print("[*] Prometheus will scrape metrics in ~30 seconds")
        print(f"[*] End Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")

def main():
    if len(sys.argv) < 2:
        print("Usage: python run_all_services_test.py <backend_path>")
        print("\nExample:")
        print("  python run_all_services_test.py D:\\cnpm\\CNPM-3\\DoAnCNPM_Backend")
        return 1
    
    backend_path = sys.argv[1]
    runner = AllServicesTestRunner(backend_path)
    total_failed = runner.run_all_services()
    
    return 0 if total_failed == 0 else 1

if __name__ == "__main__":
    sys.exit(main())
