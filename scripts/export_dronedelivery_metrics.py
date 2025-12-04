#!/usr/bin/env python3
"""
Export DroneDelivery test metrics by module (User, Drone, Order)
"""
import json
import subprocess
import sys
from pathlib import Path

def run_tests():
    """Run npm tests and get results"""
    backend_dir = Path(__file__).parent.parent / 'DroneDelivery-main' / 'BackEnd'
    
    # Run tests with JSON output and coverage
    cmd = 'npm test -- --json --outputFile=test-results.json --coverage --coverageReporters=json-summary'
    subprocess.run(cmd, shell=True, cwd=backend_dir, capture_output=True)
    
    # Parse test results
    results_file = backend_dir / 'test-results.json'
    coverage_file = backend_dir / 'coverage' / 'coverage-summary.json'
    
    if not results_file.exists():
        print("Error: test-results.json not found")
        sys.exit(1)
    
    with open(results_file, 'r', encoding='utf-8') as f:
        test_data = json.load(f)
    
    # Parse coverage
    coverage_data = {}
    if coverage_file.exists():
        with open(coverage_file, 'r', encoding='utf-8') as f:
            cov = json.load(f)
            coverage_data = cov['total']
    
    return test_data, coverage_data

def categorize_tests(test_data):
    """Categorize tests by module"""
    modules = {
        'user': {'total': 0, 'passed': 0, 'failed': 0, 'files': []},
        'drone': {'total': 0, 'passed': 0, 'failed': 0, 'files': []},
        'order': {'total': 0, 'passed': 0, 'failed': 0, 'files': []},
        'auth': {'total': 0, 'passed': 0, 'failed': 0, 'files': []}
    }
    
    for test_result in test_data.get('testResults', []):
        file_name = Path(test_result['name']).name.lower()
        
        # Determine module
        module = None
        if 'user' in file_name:
            module = 'user'
        elif 'drone' in file_name:
            module = 'drone'
        elif 'order' in file_name:
            module = 'order'
        elif 'auth' in file_name:
            module = 'auth'
        
        if module:
            passed = test_result.get('numPassingTests', 0)
            failed = test_result.get('numFailingTests', 0)
            
            modules[module]['total'] += passed + failed
            modules[module]['passed'] += passed
            modules[module]['failed'] += failed
            modules[module]['files'].append(file_name)
    
    return modules

def generate_metrics(test_data, coverage_data, modules):
    """Generate Prometheus metrics"""
    
    # Overall metrics
    total_tests = test_data.get('numTotalTests', 0)
    passed_tests = test_data.get('numPassedTests', 0)
    failed_tests = test_data.get('numFailedTests', 0)
    pass_rate = round((passed_tests / total_tests * 100) if total_tests > 0 else 0, 2)
    
    metrics = []
    
    # Overall test metrics
    metrics.append("# HELP dronedelivery_test_total Total number of tests")
    metrics.append("# TYPE dronedelivery_test_total gauge")
    metrics.append(f"dronedelivery_test_total {total_tests}")
    metrics.append("")
    
    metrics.append("# HELP dronedelivery_test_passed Number of passed tests")
    metrics.append("# TYPE dronedelivery_test_passed gauge")
    metrics.append(f"dronedelivery_test_passed {passed_tests}")
    metrics.append("")
    
    metrics.append("# HELP dronedelivery_test_failed Number of failed tests")
    metrics.append("# TYPE dronedelivery_test_failed gauge")
    metrics.append(f"dronedelivery_test_failed {failed_tests}")
    metrics.append("")
    
    metrics.append("# HELP dronedelivery_test_pass_rate Test pass rate percentage")
    metrics.append("# TYPE dronedelivery_test_pass_rate gauge")
    metrics.append(f"dronedelivery_test_pass_rate {pass_rate}")
    metrics.append("")
    
    # Module-specific metrics
    for module_name, data in modules.items():
        if data['total'] > 0:
            module_pass_rate = round((data['passed'] / data['total'] * 100), 2)
            
            metrics.append(f"# HELP dronedelivery_{module_name}_test_total Total {module_name} tests")
            metrics.append(f"# TYPE dronedelivery_{module_name}_test_total gauge")
            metrics.append(f"dronedelivery_{module_name}_test_total {data['total']}")
            metrics.append("")
            
            metrics.append(f"# HELP dronedelivery_{module_name}_test_passed Passed {module_name} tests")
            metrics.append(f"# TYPE dronedelivery_{module_name}_test_passed gauge")
            metrics.append(f"dronedelivery_{module_name}_test_passed {data['passed']}")
            metrics.append("")
            
            metrics.append(f"# HELP dronedelivery_{module_name}_test_failed Failed {module_name} tests")
            metrics.append(f"# TYPE dronedelivery_{module_name}_test_failed gauge")
            metrics.append(f"dronedelivery_{module_name}_test_failed {data['failed']}")
            metrics.append("")
            
            metrics.append(f"# HELP dronedelivery_{module_name}_test_pass_rate {module_name.title()} test pass rate")
            metrics.append(f"# TYPE dronedelivery_{module_name}_test_pass_rate gauge")
            metrics.append(f"dronedelivery_{module_name}_test_pass_rate {module_pass_rate}")
            metrics.append("")
    
    # Coverage metrics
    if coverage_data:
        for metric_name, value in [
            ('statements', coverage_data.get('statements', {}).get('pct', 0)),
            ('branches', coverage_data.get('branches', {}).get('pct', 0)),
            ('functions', coverage_data.get('functions', {}).get('pct', 0)),
            ('lines', coverage_data.get('lines', {}).get('pct', 0))
        ]:
            metrics.append(f"# HELP dronedelivery_coverage_{metric_name} {metric_name.title()} coverage percentage")
            metrics.append(f"# TYPE dronedelivery_coverage_{metric_name} gauge")
            metrics.append(f"dronedelivery_coverage_{metric_name} {value}")
            metrics.append("")
    
    return "\n".join(metrics)

def main():
    print("Running DroneDelivery tests...")
    test_data, coverage_data = run_tests()
    
    print("Categorizing tests by module...")
    modules = categorize_tests(test_data)
    
    # Print summary
    print("\n=== Test Summary by Module ===")
    for module_name, data in modules.items():
        if data['total'] > 0:
            pass_rate = round((data['passed'] / data['total'] * 100), 2)
            print(f"{module_name.upper()}: {data['passed']}/{data['total']} passed ({pass_rate}%)")
            print(f"  Files: {', '.join(data['files'])}")
    
    print(f"\nOVERALL: {test_data['numPassedTests']}/{test_data['numTotalTests']} passed")
    
    print("\nGenerating Prometheus metrics...")
    metrics_content = generate_metrics(test_data, coverage_data, modules)
    
    # Write metrics file
    metrics_file = Path(__file__).parent.parent / 'monitoring' / 'metrics' / 'dronedelivery_test_metrics.txt'
    metrics_file.parent.mkdir(parents=True, exist_ok=True)
    
    with open(metrics_file, 'w', encoding='utf-8') as f:
        f.write(metrics_content)
    
    print(f"✓ Metrics exported to {metrics_file}")
    print("\nRestart metrics-server to load new data:")
    print("  docker compose restart metrics-server")

if __name__ == '__main__':
    main()
