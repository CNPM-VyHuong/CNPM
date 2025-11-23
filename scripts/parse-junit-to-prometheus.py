#!/usr/bin/env python3
"""
Parse JUnit XML test results and export as Prometheus metrics
"""
import xml.etree.ElementTree as ET
import os
import glob
from datetime import datetime
from pathlib import Path

def parse_junit_files():
    """Parse all JUnit XML files and generate Prometheus metrics"""
    
    metrics = []
    timestamp = int(datetime.now().timestamp() * 1000)
    
    # Find all surefire reports
    junit_paths = glob.glob('**/target/surefire-reports/*.xml', recursive=True)
    
    for junit_file in junit_paths:
        try:
            tree = ET.parse(junit_file)
            root = tree.getroot()
            
            # Extract test suite info
            service_name = os.path.dirname(junit_file).split(os.sep)[0]
            
            tests = int(root.get('tests', 0))
            failures = int(root.get('failures', 0))
            errors = int(root.get('errors', 0))
            skipped = int(root.get('skipped', 0))
            time_taken = float(root.get('time', 0))
            
            success = tests - failures - errors - skipped
            
            # Generate Prometheus metrics
            metrics.append(f'# HELP unit_tests_total Total number of unit tests')
            metrics.append(f'# TYPE unit_tests_total gauge')
            metrics.append(f'unit_tests_total{{service="{service_name}"}} {tests} {timestamp}')
            
            metrics.append(f'# HELP unit_tests_passed Number of passed tests')
            metrics.append(f'# TYPE unit_tests_passed gauge')
            metrics.append(f'unit_tests_passed{{service="{service_name}"}} {success} {timestamp}')
            
            metrics.append(f'# HELP unit_tests_failed Number of failed tests')
            metrics.append(f'# TYPE unit_tests_failed gauge')
            metrics.append(f'unit_tests_failed{{service="{service_name}"}} {failures} {timestamp}')
            
            metrics.append(f'# HELP unit_tests_errors Number of test errors')
            metrics.append(f'# TYPE unit_tests_errors gauge')
            metrics.append(f'unit_tests_errors{{service="{service_name}"}} {errors} {timestamp}')
            
            metrics.append(f'# HELP unit_tests_skipped Number of skipped tests')
            metrics.append(f'# TYPE unit_tests_skipped gauge')
            metrics.append(f'unit_tests_skipped{{service="{service_name}"}} {skipped} {timestamp}')
            
            metrics.append(f'# HELP unit_tests_duration_seconds Test execution time')
            metrics.append(f'# TYPE unit_tests_duration_seconds gauge')
            metrics.append(f'unit_tests_duration_seconds{{service="{service_name}"}} {time_taken} {timestamp}')
            
            success_rate = (success / tests * 100) if tests > 0 else 0
            metrics.append(f'# HELP unit_tests_success_rate Test success rate percentage')
            metrics.append(f'# TYPE unit_tests_success_rate gauge')
            metrics.append(f'unit_tests_success_rate{{service="{service_name}"}} {success_rate} {timestamp}')
            
            print(f"✓ Parsed {service_name}: {success}/{tests} passed")
            
        except Exception as e:
            print(f"✗ Error parsing {junit_file}: {e}")
    
    # Write metrics file
    metrics_dir = Path('monitoring/metrics')
    metrics_dir.mkdir(parents=True, exist_ok=True)
    
    metrics_file = metrics_dir / 'unit-tests.prom'
    with open(metrics_file, 'w') as f:
        f.write('\n'.join(metrics))
    
    print(f"\n✓ Metrics written to {metrics_file}")
    return metrics_file

if __name__ == '__main__':
    parse_junit_files()
