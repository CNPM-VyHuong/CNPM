#!/usr/bin/env python3
"""
DroneDelivery Test Runner - Runs Jest tests and exports metrics
"""

import subprocess
import json
import os
import sys
from pathlib import Path
from datetime import datetime

class DroneDeliveryTestRunner:
    def __init__(self, project_path):
        self.project_path = Path(project_path)
        self.backend_path = self.project_path / "BackEnd"
        self.coverage_file = self.backend_path / "coverage" / "coverage-summary.json"
        self.metrics_output = Path(__file__).parent.parent / "monitoring" / "metrics" / "dronedelivery_test_metrics.txt"
        
    def run_tests(self):
        """Run Jest tests with coverage"""
        print("🧪 Running DroneDelivery tests...")
        
        try:
            # Change to backend directory
            os.chdir(self.backend_path)
            
            # Run npm test
            result = subprocess.run(
                ["npm", "test", "--", "--coverage", "--json", "--outputFile=test-results.json"],
                capture_output=True,
                text=True,
                timeout=300
            )
            
            print(f"✅ Tests completed with exit code: {result.returncode}")
            
            return result.returncode == 0
            
        except subprocess.TimeoutExpired:
            print("❌ Tests timed out after 5 minutes")
            return False
        except Exception as e:
            print(f"❌ Error running tests: {e}")
            return False
    
    def parse_coverage(self):
        """Parse coverage summary"""
        if not self.coverage_file.exists():
            print(f"⚠️ Coverage file not found: {self.coverage_file}")
            return None
            
        try:
            with open(self.coverage_file, 'r') as f:
                coverage = json.load(f)
            
            total = coverage.get('total', {})
            return {
                'statements': total.get('statements', {}).get('pct', 0),
                'branches': total.get('branches', {}).get('pct', 0),
                'functions': total.get('functions', {}).get('pct', 0),
                'lines': total.get('lines', {}).get('pct', 0),
            }
        except Exception as e:
            print(f"❌ Error parsing coverage: {e}")
            return None
    
    def parse_test_results(self):
        """Parse test results"""
        test_results_file = self.backend_path / "test-results.json"
        
        if not test_results_file.exists():
            print(f"⚠️ Test results file not found: {test_results_file}")
            return None
        
        try:
            with open(test_results_file, 'r') as f:
                results = json.load(f)
            
            return {
                'numTotalTests': results.get('numTotalTests', 0),
                'numPassedTests': results.get('numPassedTests', 0),
                'numFailedTests': results.get('numFailedTests', 0),
                'numPendingTests': results.get('numPendingTests', 0),
                'success': results.get('success', False),
            }
        except Exception as e:
            print(f"❌ Error parsing test results: {e}")
            return None
    
    def export_metrics(self, coverage, test_results):
        """Export metrics in Prometheus format"""
        if not coverage or not test_results:
            print("⚠️ No metrics to export")
            return False
        
        timestamp = int(datetime.now().timestamp() * 1000)
        
        metrics = []
        
        # Test metrics
        metrics.append("# HELP dronedelivery_test_total Total number of tests")
        metrics.append("# TYPE dronedelivery_test_total gauge")
        metrics.append(f"dronedelivery_test_total {test_results['numTotalTests']} {timestamp}")
        
        metrics.append("# HELP dronedelivery_test_passed Number of passed tests")
        metrics.append("# TYPE dronedelivery_test_passed gauge")
        metrics.append(f"dronedelivery_test_passed {test_results['numPassedTests']} {timestamp}")
        
        metrics.append("# HELP dronedelivery_test_failed Number of failed tests")
        metrics.append("# TYPE dronedelivery_test_failed gauge")
        metrics.append(f"dronedelivery_test_failed {test_results['numFailedTests']} {timestamp}")
        
        metrics.append("# HELP dronedelivery_test_pass_rate Test pass rate percentage")
        metrics.append("# TYPE dronedelivery_test_pass_rate gauge")
        pass_rate = (test_results['numPassedTests'] / test_results['numTotalTests'] * 100) if test_results['numTotalTests'] > 0 else 0
        metrics.append(f"dronedelivery_test_pass_rate {pass_rate:.2f} {timestamp}")
        
        # Coverage metrics
        metrics.append("# HELP dronedelivery_coverage_statements Statement coverage percentage")
        metrics.append("# TYPE dronedelivery_coverage_statements gauge")
        metrics.append(f"dronedelivery_coverage_statements {coverage['statements']} {timestamp}")
        
        metrics.append("# HELP dronedelivery_coverage_branches Branch coverage percentage")
        metrics.append("# TYPE dronedelivery_coverage_branches gauge")
        metrics.append(f"dronedelivery_coverage_branches {coverage['branches']} {timestamp}")
        
        metrics.append("# HELP dronedelivery_coverage_functions Function coverage percentage")
        metrics.append("# TYPE dronedelivery_coverage_functions gauge")
        metrics.append(f"dronedelivery_coverage_functions {coverage['functions']} {timestamp}")
        
        metrics.append("# HELP dronedelivery_coverage_lines Line coverage percentage")
        metrics.append("# TYPE dronedelivery_coverage_lines gauge")
        metrics.append(f"dronedelivery_coverage_lines {coverage['lines']} {timestamp}")
        
        # Write metrics file
        try:
            self.metrics_output.parent.mkdir(parents=True, exist_ok=True)
            with open(self.metrics_output, 'w') as f:
                f.write('\n'.join(metrics) + '\n')
            
            print(f"✅ Metrics exported to: {self.metrics_output}")
            return True
        except Exception as e:
            print(f"❌ Error exporting metrics: {e}")
            return False
    
    def run(self):
        """Run the complete test pipeline"""
        print("=" * 60)
        print("🚁 DroneDelivery Test Runner")
        print("=" * 60)
        
        # Run tests
        success = self.run_tests()
        
        # Parse results
        coverage = self.parse_coverage()
        test_results = self.parse_test_results()
        
        # Export metrics
        if coverage and test_results:
            self.export_metrics(coverage, test_results)
        
        # Print summary
        print("\n" + "=" * 60)
        print("📊 Test Summary")
        print("=" * 60)
        
        if test_results:
            print(f"Total Tests: {test_results['numTotalTests']}")
            print(f"✅ Passed: {test_results['numPassedTests']}")
            print(f"❌ Failed: {test_results['numFailedTests']}")
            print(f"⏸️  Pending: {test_results['numPendingTests']}")
            print(f"Pass Rate: {pass_rate:.2f}%")
        
        if coverage:
            print(f"\n📈 Coverage:")
            print(f"Statements: {coverage['statements']}%")
            print(f"Branches: {coverage['branches']}%")
            print(f"Functions: {coverage['functions']}%")
            print(f"Lines: {coverage['lines']}%")
        
        print("=" * 60)
        
        return 0 if success else 1

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python run_dronedelivery_tests.py <project_path>")
        sys.exit(1)
    
    project_path = sys.argv[1]
    runner = DroneDeliveryTestRunner(project_path)
    sys.exit(runner.run())
