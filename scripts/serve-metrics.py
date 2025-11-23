#!/usr/bin/env python3
"""
Simple HTTP server to expose JUnit metrics to Prometheus
Usage: python serve-metrics.py
Then configure Prometheus to scrape: http://localhost:9091/metrics
"""
from http.server import HTTPServer, BaseHTTPRequestHandler
from pathlib import Path
import os

METRICS_FILE = Path(__file__).parent.parent / 'monitoring' / 'metrics' / 'unit-tests.prom'

class MetricsHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/metrics':
            if METRICS_FILE.exists():
                with open(METRICS_FILE, 'r') as f:
                    metrics = f.read()
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain; charset=utf-8')
                self.end_headers()
                self.wfile.write(metrics.encode('utf-8'))
            else:
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b'No metrics available yet. Run tests first.\n')
        else:
            self.send_response(404)
            self.end_headers()
    
    def log_message(self, format, *args):
        # Suppress default logging
        pass

if __name__ == '__main__':
    port = 9091
    server = HTTPServer(('0.0.0.0', port), MetricsHandler)
    print(f"✓ Metrics server running on http://0.0.0.0:{port}/metrics")
    print(f"✓ Configure Prometheus to scrape: http://localhost:{port}/metrics")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n✓ Metrics server stopped")
        server.server_close()
