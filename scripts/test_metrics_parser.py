#!/usr/bin/env python3
"""
Test Metrics Parser - Extracts test results from JUnit XML and exports to Prometheus
Parses Maven test reports and pushes metrics to Prometheus Push Gateway
"""

import os
import sys
import json
import glob
import xml.etree.ElementTree as ET
from pathlib import Path
from datetime import datetime
import requests
from typing import Dict, List, Tuple

class TestMetricsParser:
    def __init__(self, backend_path: str):
        self.backend_path = Path(backend_path)
        self.metrics = {}
        self.services = [
            'user_service',
            'product_service',
            'drone_service',
            'order_service',
            'payment_service',
            'restaurant-service'
        ]
    
    def find_test_reports(self, service: str) -> List[Path]:
        """Find all TEST-*.xml files in a service"""
        pattern = self.backend_path / service / "target" / "surefire-reports" / "TEST-*.xml"
        return list(glob.glob(str(pattern)))
    
    def parse_test_file(self, file_path: str) -> Tuple[int, int, int, float]:
        """
        Parse JUnit XML test report
        Returns: (total_tests, passed_tests, failed_tests, execution_time)
        """
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            
            # Extract metrics from testsuite element
            tests = int(root.get('tests', 0))
            failures = int(root.get('failures', 0))
            errors = int(root.get('errors', 0))
            time = float(root.get('time', 0))
            
            # Passed = total - failures - errors
            passed = tests - failures - errors
            
            return tests, passed, failures + errors, time
        except Exception as e:
            print(f"Error parsing {file_path}: {e}")
            return 0, 0, 0, 0
    
    def collect_metrics(self) -> Dict:
        """Collect test metrics from all services"""
        all_metrics = {
            'timestamp': datetime.now().isoformat(),
            'services': {}
        }
        
        total_tests = 0
        total_passed = 0
        total_failed = 0
        total_time = 0
        
        for service in self.services:
            service_key = service.replace('-', '_')
            test_files = self.find_test_reports(service)
            
            service_tests = 0
            service_passed = 0
            service_failed = 0
            service_time = 0
            test_class_count = 0
            
            for test_file in test_files:
                tests, passed, failed, exec_time = self.parse_test_file(test_file)
                service_tests += tests
                service_passed += passed
                service_failed += failed
                service_time += exec_time
                test_class_count += 1
            
            # Calculate pass rate
            pass_rate = (service_passed / service_tests * 100) if service_tests > 0 else 0
            
            all_metrics['services'][service_key] = {
                'total_tests': service_tests,
                'passed_tests': service_passed,
                'failed_tests': service_failed,
                'execution_time_sec': round(service_time, 2),
                'pass_rate_percent': round(pass_rate, 2),
                'test_classes': test_class_count,
                'status': 'PASS' if service_failed == 0 else 'FAIL'
            }
            
            total_tests += service_tests
            total_passed += service_passed
            total_failed += service_failed
            total_time += service_time
        
        # Add summary
        overall_pass_rate = (total_passed / total_tests * 100) if total_tests > 0 else 0
        all_metrics['summary'] = {
            'total_tests': total_tests,
            'total_passed': total_passed,
            'total_failed': total_failed,
            'total_time_sec': round(total_time, 2),
            'pass_rate_percent': round(overall_pass_rate, 2),
            'services_count': len(self.services),
            'status': 'PASS' if total_failed == 0 else 'FAIL'
        }
        
        return all_metrics
    
    def export_prometheus_format(self, metrics: Dict) -> str:
        """Export metrics in Prometheus text format"""
        lines = []
        timestamp = int(datetime.now().timestamp() * 1000)
        
        # Summary metrics
        summary = metrics['summary']
        lines.append(f"# HELP test_count_total Total number of tests")
        lines.append(f"# TYPE test_count_total counter")
        lines.append(f"test_count_total {summary['total_tests']} {timestamp}")
        
        lines.append(f"# HELP test_pass_count Total number of passed tests")
        lines.append(f"# TYPE test_pass_count counter")
        lines.append(f"test_pass_count {summary['total_passed']} {timestamp}")
        
        lines.append(f"# HELP test_fail_count Total number of failed tests")
        lines.append(f"# TYPE test_fail_count counter")
        lines.append(f"test_fail_count {summary['total_failed']} {timestamp}")
        
        lines.append(f"# HELP test_pass_rate_percent Overall test pass rate")
        lines.append(f"# TYPE test_pass_rate_percent gauge")
        lines.append(f"test_pass_rate_percent {summary['pass_rate_percent']} {timestamp}")
        
        lines.append(f"# HELP test_execution_time_seconds Total test execution time")
        lines.append(f"# TYPE test_execution_time_seconds gauge")
        lines.append(f"test_execution_time_seconds {summary['total_time_sec']} {timestamp}")
        
        # Per-service metrics
        lines.append(f"# HELP test_count_by_service Test count by service")
        lines.append(f"# TYPE test_count_by_service gauge")
        
        for service, data in metrics['services'].items():
            lines.append(f'test_count_by_service{{service="{service}"}} {data["total_tests"]} {timestamp}')
            lines.append(f'test_pass_count_by_service{{service="{service}"}} {data["passed_tests"]} {timestamp}')
            lines.append(f'test_fail_count_by_service{{service="{service}"}} {data["failed_tests"]} {timestamp}')
            lines.append(f'test_pass_rate_by_service{{service="{service}"}} {data["pass_rate_percent"]} {timestamp}')
            lines.append(f'test_execution_time_by_service{{service="{service}"}} {data["execution_time_sec"]} {timestamp}')
        
        return '\n'.join(lines)
    
    def save_metrics_json(self, metrics: Dict, output_file: str):
        """Save metrics to JSON file"""
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        with open(output_file, 'w') as f:
            json.dump(metrics, f, indent=2)
        print(f"✅ Metrics saved to {output_file}")
    
    def push_to_prometheus(self, prometheus_url: str, prometheus_port: int, job_name: str):
        """Push metrics to Prometheus Push Gateway"""
        push_gateway_url = f"http://{prometheus_url}:{prometheus_port}/metrics/job/{job_name}"
        
        try:
            # For this example, we'll save to a file instead
            # In production, you would use: requests.post(push_gateway_url, data=prometheus_data)
            print(f"📊 Prometheus Push Gateway URL: {push_gateway_url}")
            print("   (Metrics saved to file for visualization)")
        except Exception as e:
            print(f"⚠️  Warning: Could not push to Prometheus: {e}")
    
    def print_summary(self, metrics: Dict):
        """Print summary to console"""
        summary = metrics['summary']
        print("\n" + "="*60)
        print("📊 TEST EXECUTION SUMMARY")
        print("="*60)
        print(f"Total Tests: {summary['total_tests']}")
        print(f"Passed: {summary['total_passed']} ✅")
        print(f"Failed: {summary['total_failed']} ❌")
        print(f"Pass Rate: {summary['pass_rate_percent']}%")
        print(f"Total Time: {summary['total_time_sec']}s")
        print(f"Status: {summary['status']}")
        print("="*60)
        print("\n📈 SERVICE BREAKDOWN:")
        print("-"*60)
        
        for service, data in sorted(metrics['services'].items()):
            status_icon = "✅" if data['status'] == 'PASS' else "❌"
            print(f"{status_icon} {service.upper()}")
            print(f"   Tests: {data['total_tests']} | Pass: {data['passed_tests']} | Fail: {data['failed_tests']}")
            print(f"   Pass Rate: {data['pass_rate_percent']}% | Time: {data['execution_time_sec']}s")
        
        print("-"*60 + "\n")

def main():
    # Get backend path from command line or use default
    backend_path = sys.argv[1] if len(sys.argv) > 1 else "D:\\cnpm\\CNPM-3\\DoAnCNPM_Backend"
    
    print(f"🔍 Parsing test reports from: {backend_path}")
    print("="*60)
    
    parser = TestMetricsParser(backend_path)
    
    # Collect metrics
    metrics = parser.collect_metrics()
    
    # Save metrics to JSON
    json_output = os.path.join(os.path.dirname(backend_path), "monitoring", "metrics", "test_metrics.json")
    parser.save_metrics_json(metrics, json_output)
    
    # Export to Prometheus format
    prometheus_output = os.path.join(os.path.dirname(backend_path), "monitoring", "metrics", "test_metrics.txt")
    prometheus_data = parser.export_prometheus_format(metrics)
    os.makedirs(os.path.dirname(prometheus_output), exist_ok=True)
    with open(prometheus_output, 'w') as f:
        f.write(prometheus_data)
    print(f"✅ Prometheus metrics saved to {prometheus_output}")
    
    # Print summary
    parser.print_summary(metrics)
    
    # Optionally push to Prometheus (commented out for file-based setup)
    # parser.push_to_prometheus("localhost", 9091, "test-metrics")
    
    return 0 if metrics['summary']['total_failed'] == 0 else 1

if __name__ == "__main__":
    sys.exit(main())
