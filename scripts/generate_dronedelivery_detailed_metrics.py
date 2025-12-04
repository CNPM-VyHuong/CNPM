#!/usr/bin/env python3
"""
Generate detailed DroneDelivery test metrics with test suite names
"""

import json
from pathlib import Path
from datetime import datetime

def generate_detailed_metrics():
    """Generate Prometheus metrics with test suite details"""
    
    metrics_lines = []
    
    # Test Suite Details
    test_suites = [
        {
            "name": "User Model Unit Tests",
            "passed": 10,
            "failed": 0,
            "total": 10,
            "status": "✅ PASSED"
        },
        {
            "name": "Drone Model Unit Tests",
            "passed": 10,
            "failed": 0,
            "total": 10,
            "status": "✅ PASSED"
        },
        {
            "name": "Auth API Integration Tests",
            "passed": 7,
            "failed": 0,
            "total": 7,
            "status": "✅ PASSED"
        },
        {
            "name": "Order Model Unit Tests",
            "passed": 6,
            "failed": 1,
            "total": 7,
            "status": "❌ FAILED"
        },
        {
            "name": "Drone API Integration Tests",
            "passed": 2,
            "failed": 6,
            "total": 8,
            "status": "❌ FAILED"
        },
        {
            "name": "Order API Integration Tests",
            "passed": 2,
            "failed": 2,
            "total": 4,
            "status": "❌ FAILED"
        }
    ]
    
    # Overall metrics
    total_passed = sum(s["passed"] for s in test_suites)
    total_failed = sum(s["failed"] for s in test_suites)
    total_tests = sum(s["total"] for s in test_suites)
    pass_rate = (total_passed / total_tests * 100) if total_tests > 0 else 0
    
    # Add overall metrics
    metrics_lines.append("# HELP dronedelivery_test_total Total number of tests")
    metrics_lines.append("# TYPE dronedelivery_test_total gauge")
    metrics_lines.append(f"dronedelivery_test_total {total_tests}")
    
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_test_passed Number of passed tests")
    metrics_lines.append("# TYPE dronedelivery_test_passed gauge")
    metrics_lines.append(f"dronedelivery_test_passed {total_passed}")
    
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_test_failed Number of failed tests")
    metrics_lines.append("# TYPE dronedelivery_test_failed gauge")
    metrics_lines.append(f"dronedelivery_test_failed {total_failed}")
    
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_test_pass_rate Test pass rate percentage")
    metrics_lines.append("# TYPE dronedelivery_test_pass_rate gauge")
    metrics_lines.append(f"dronedelivery_test_pass_rate {pass_rate:.2f}")
    
    # Add per-suite metrics
    for suite in test_suites:
        safe_name = suite["name"].lower().replace(" ", "_").replace("(", "").replace(")", "").replace(".", "")
        
        metrics_lines.append("")
        metrics_lines.append(f"# HELP dronedelivery_suite_{safe_name}_passed {suite['name']} passed tests")
        metrics_lines.append(f"# TYPE dronedelivery_suite_{safe_name}_passed gauge")
        metrics_lines.append(f'dronedelivery_suite_{safe_name}_passed{{suite="{suite["name"]}"}} {suite["passed"]}')
        
        metrics_lines.append("")
        metrics_lines.append(f"# HELP dronedelivery_suite_{safe_name}_failed {suite['name']} failed tests")
        metrics_lines.append(f"# TYPE dronedelivery_suite_{safe_name}_failed gauge")
        metrics_lines.append(f'dronedelivery_suite_{safe_name}_failed{{suite="{suite["name"]}"}} {suite["failed"]}')
    
    # Coverage metrics
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_coverage_statements Statements coverage percentage")
    metrics_lines.append("# TYPE dronedelivery_coverage_statements gauge")
    metrics_lines.append("dronedelivery_coverage_statements 10.32")
    
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_coverage_branches Branches coverage percentage")
    metrics_lines.append("# TYPE dronedelivery_coverage_branches gauge")
    metrics_lines.append("dronedelivery_coverage_branches 3.03")
    
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_coverage_functions Functions coverage percentage")
    metrics_lines.append("# TYPE dronedelivery_coverage_functions gauge")
    metrics_lines.append("dronedelivery_coverage_functions 10.61")
    
    metrics_lines.append("")
    metrics_lines.append("# HELP dronedelivery_coverage_lines Lines coverage percentage")
    metrics_lines.append("# TYPE dronedelivery_coverage_lines gauge")
    metrics_lines.append("dronedelivery_coverage_lines 10.42")
    
    return "\n".join(metrics_lines), test_suites

def main():
    # Generate metrics
    metrics, test_suites = generate_detailed_metrics()
    
    # Ensure monitoring/metrics directory exists
    metrics_dir = Path(__file__).parent.parent / "monitoring" / "metrics"
    metrics_dir.mkdir(parents=True, exist_ok=True)
    
    # Write to file
    output_file = metrics_dir / "dronedelivery_test_metrics.txt"
    with open(output_file, 'w') as f:
        f.write(metrics)
    
    print(f"✅ Detailed metrics exported to: {output_file}\n")
    
    # Print summary
    print("📊 Test Suites Summary:")
    print("=" * 80)
    print(f"{'Test Suite':<40} {'Passed':<10} {'Failed':<10} {'Status':<15}")
    print("=" * 80)
    
    for suite in test_suites:
        print(f"{suite['name']:<40} {suite['passed']:<10} {suite['failed']:<10} {suite['status']:<15}")
    
    print("=" * 80)
    total_passed = sum(s["passed"] for s in test_suites)
    total_failed = sum(s["failed"] for s in test_suites)
    total_tests = total_passed + total_failed
    print(f"{'TOTAL':<40} {total_passed:<10} {total_failed:<10} {'80.43% Pass Rate':<15}")
    print("=" * 80)

if __name__ == "__main__":
    main()
