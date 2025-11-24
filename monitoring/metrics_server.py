#!/usr/bin/env python3
"""
Simple HTTP server to expose test metrics to Prometheus
"""
from http.server import HTTPServer, BaseHTTPRequestHandler
import os
import json
from datetime import datetime

METRICS_FILE = '/metrics/test_metrics.txt'

class MetricsHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/metrics':
            try:
                # Read metrics from file
                if os.path.exists(METRICS_FILE):
                    with open(METRICS_FILE, 'r') as f:
                        metrics_content = f.read()
                else:
                    metrics_content = "# No metrics available yet\n"
                
                self.send_response(200)
                self.send_header('Content-type', 'text/plain; charset=utf-8')
                self.end_headers()
                self.wfile.write(metrics_content.encode('utf-8'))
            except Exception as e:
                self.send_response(500)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(f"Error reading metrics: {str(e)}".encode('utf-8'))
        elif self.path == '/health':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = {'status': 'ok', 'timestamp': datetime.now().isoformat()}
            self.wfile.write(json.dumps(response).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()
    
    def log_message(self, format, *args):
        print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {format % args}")

if __name__ == '__main__':
    server_address = ('0.0.0.0', 9091)
    httpd = HTTPServer(server_address, MetricsHandler)
    print(f"Metrics server started on port 9091")
    print(f"Metrics endpoint: http://localhost:9091/metrics")
    print(f"Health check: http://localhost:9091/health")
    httpd.serve_forever()
