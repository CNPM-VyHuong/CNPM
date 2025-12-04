#!/usr/bin/env python3
"""
Run tests for a single service and update metrics in real-time
Supports individual project testing with live Grafana dashboard updates
"""

import os
import sys
import subprocess
import json
import glob
import xml.etree.ElementTree as ET
from pathlib import Path
from datetime import datetime
from typing import Tuple

class SingleServiceTestRunner:
    def __init__(self, backend_path: str, service_name: str):
        self.backend_path = Path(backend_path)
        self.service_path = self.backend_path / service_name
        self.service_name = service_name
        self.metrics_dir = self.backend_path.parent / "monitoring" / "metrics"
        
    def run_tests(self) -> bool:
        """Run Maven tests for the service"""
        print(f"\n{'='*70}")
        print(f"[*] Running tests for: {self.service_name.upper()}")
        print(f"{'='*70}")
        print(f"[*] Path: {self.service_path}")
        
        if not self.service_path.exists():
            print(f"[!] ERROR: Service path does not exist: {self.service_path}")
            return False
        
        # Use Maven wrapper
        mvnw_cmd = self.service_path / "mvnw.cmd"
        if mvnw_cmd.exists():
            cmd = ["cmd", "/c", str(mvnw_cmd), "test", "-q"]
        else:
            cmd = ["mvn", "test", "-q"]
        
        print(f"[*] Running: {' '.join(cmd)}")
        print(f"{'='*70}\n")
        
        try:
            result = subprocess.run(
                cmd,
                cwd=str(self.service_path),
                capture_output=False
            )
            
            if result.returncode == 0:
                print(f"\n{'='*70}")
                print(f"[✓] Tests PASSED for {self.service_name}")
                print(f"{'='*70}\n")
                return True
            else:
                print(f"\n{'='*70}")
                print(f"[✗] Tests FAILED for {self.service_name}")
                print(f"{'='*70}\n")
                return False
                
        except Exception as e:
            print(f"[!] Error running tests: {e}")
            return False
    
    def parse_test_results(self) -> Tuple[int, int, int, float]:
        """Parse JUnit XML test reports"""
        surefire_path = self.service_path / "target" / "surefire-reports"
        
        if not surefire_path.exists():
            print(f"[!] No test reports found at: {surefire_path}")
            return 0, 0, 0, 0
        
        # Find TEST-*.xml files
        test_files = list(glob.glob(str(surefire_path / "TEST-*.xml")))
        
        if not test_files:
            print(f"[!] No TEST-*.xml files found")
            return 0, 0, 0, 0
        
        total_tests = 0
        total_passed = 0
        total_failed = 0
        total_time = 0
        
        for test_file in test_files:
            try:
                tree = ET.parse(test_file)
                root = tree.getroot()
                
                tests = int(root.get('tests', 0))
                failures = int(root.get('failures', 0))
                errors = int(root.get('errors', 0))
                time = float(root.get('time', 0))
                
                passed = tests - failures - errors
                
                total_tests += tests
                total_passed += passed
                total_failed += failures + errors
                total_time += time
                
                print(f"[+] {Path(test_file).name}: {tests} tests, {passed} passed, {failures + errors} failed ({time:.2f}s)")
                
            except Exception as e:
                print(f"[!] Error parsing {test_file}: {e}")
        
        return total_tests, total_passed, total_failed, total_time
    
    def update_metrics_file(self, tests: int, passed: int, failed: int, exec_time: float):
        """Update the metrics file with new data"""
        self.metrics_dir.mkdir(parents=True, exist_ok=True)
        metrics_file = self.metrics_dir / "test_metrics.txt"
        
        # Read existing metrics
        all_metrics = {}
        if metrics_file.exists():
            with open(metrics_file, 'r') as f:
                content = f.read()
                # Parse existing metrics (simplified)
                for line in content.split('\n'):
                    if line.startswith('test_') and '{' in line and '}' in line:
                        all_metrics[line] = True
        
        timestamp = int(datetime.now().timestamp() * 1000)
        
        # Build Prometheus format with updated service data
        lines = []
        
        # Summary metrics (keep existing)
        lines.append(f"# HELP test_count_total Total number of tests")
        lines.append(f"# TYPE test_count_total counter")
        lines.append(f"test_count_total {tests} {timestamp}")
        
        lines.append(f"# HELP test_pass_count Total number of passed tests")
        lines.append(f"# TYPE test_pass_count counter")
        lines.append(f"test_pass_count {passed} {timestamp}")
        
        lines.append(f"# HELP test_fail_count Total number of failed tests")
        lines.append(f"# TYPE test_fail_count counter")
        lines.append(f"test_fail_count {failed} {timestamp}")
        
        pass_rate = (passed / tests * 100) if tests > 0 else 0
        lines.append(f"# HELP test_pass_rate_percent Overall test pass rate")
        lines.append(f"# TYPE test_pass_rate_percent gauge")
        lines.append(f"test_pass_rate_percent {pass_rate} {timestamp}")
        
        lines.append(f"# HELP test_execution_time_seconds Total test execution time")
        lines.append(f"# TYPE test_execution_time_seconds gauge")
        lines.append(f"test_execution_time_seconds {exec_time} {timestamp}")
        
        # Per-service metrics
        lines.append(f"# HELP test_count_by_service Test count by service")
        lines.append(f"# TYPE test_count_by_service gauge")
        lines.append(f'test_count_by_service{{service="{self.service_name}"}} {tests} {timestamp}')
        lines.append(f'test_pass_count_by_service{{service="{self.service_name}"}} {passed} {timestamp}')
        lines.append(f'test_fail_count_by_service{{service="{self.service_name}"}} {failed} {timestamp}')
        lines.append(f'test_pass_rate_by_service{{service="{self.service_name}"}} {pass_rate} {timestamp}')
        lines.append(f'test_execution_time_by_service{{service="{self.service_name}"}} {exec_time} {timestamp}')
        
        # Save metrics
        with open(metrics_file, 'w') as f:
            f.write('\n'.join(lines))
        
        print(f"[+] Metrics updated: {metrics_file}")
    
    def save_json_report(self, tests: int, passed: int, failed: int, exec_time: float):
        """Save test report as JSON"""
        self.metrics_dir.mkdir(parents=True, exist_ok=True)
        json_file = self.metrics_dir / f"test_report_{self.service_name}.json"
        
        pass_rate = (passed / tests * 100) if tests > 0 else 0
        
        report = {
            "service": self.service_name,
            "timestamp": datetime.now().isoformat(),
            "metrics": {
                "total_tests": tests,
                "passed_tests": passed,
                "failed_tests": failed,
                "execution_time_seconds": round(exec_time, 2),
                "pass_rate_percent": round(pass_rate, 2),
                "status": "PASS" if failed == 0 else "FAIL"
            }
        }
        
        with open(json_file, 'w') as f:
            json.dump(report, f, indent=2)
        
        print(f"[+] JSON report saved: {json_file}")
    
    def print_results(self, tests: int, passed: int, failed: int, exec_time: float):
        """Print formatted test results"""
        if tests == 0:
            print("[!] No tests found")
            return
        
        pass_rate = (passed / tests * 100)
        status = "✓ PASS" if failed == 0 else "✗ FAIL"
        
        print(f"\n{'='*70}")
        print(f"[*] TEST RESULTS FOR: {self.service_name.upper()}")
        print(f"{'='*70}")
        print(f"Total Tests:     {tests}")
        print(f"Passed:          {passed} [✓]")
        print(f"Failed:          {failed} [✗]")
        print(f"Pass Rate:       {pass_rate:.2f}%")
        print(f"Execution Time:  {exec_time:.2f}s")
        print(f"Status:          {status}")
        print(f"{'='*70}\n")
    
    def run(self) -> int:
        """Main execution flow"""
        # Run tests
        test_success = self.run_tests()
        
        if not test_success:
            print("[!] Tests failed during execution")
            return 1
        
        # Parse results
        tests, passed, failed, exec_time = self.parse_test_results()
        
        if tests == 0:
            print("[!] Could not parse test results")
            return 1
        
        # Print results
        self.print_results(tests, passed, failed, exec_time)
        
        # Update metrics
        self.update_metrics_file(tests, passed, failed, exec_time)
        
        # Save JSON report
        self.save_json_report(tests, passed, failed, exec_time)
        
        print("[✓] Grafana dashboard will update in 30 seconds (Prometheus scrape interval)")
        
        return 0 if failed == 0 else 1

def main():
    if len(sys.argv) < 2:
        print("Usage: python run_single_service_test.py <backend_path> <service_name>")
        print("\nExamples:")
        print("  python run_single_service_test.py D:\\cnpm\\CNPM-3\\DoAnCNPM_Backend user_service")
        print("  python run_single_service_test.py D:\\cnpm\\CNPM-3\\DoAnCNPM_Backend product_service")
        print("  python run_single_service_test.py D:\\cnpm\\CNPM-3\\DoAnCNPM_Backend drone_service")
        print("\nAvailable services:")
        print("  - user_service")
        print("  - product_service")
        print("  - drone_service")
        print("  - order_service")
        print("  - payment_service")
        print("  - restaurant-service")
        return 1
    
    backend_path = sys.argv[1]
    service_name = sys.argv[2]
    
    runner = SingleServiceTestRunner(backend_path, service_name)
    return runner.run()

if __name__ == "__main__":
    sys.exit(main())
