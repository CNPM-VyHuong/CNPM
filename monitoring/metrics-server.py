#!/usr/bin/env python3
"""
Metrics Server - Expose test metrics to Prometheus
Reads test_metrics.txt and serves it via HTTP endpoint
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import os
from pathlib import Path
from datetime import datetime

class MetricsHandler(BaseHTTPRequestHandler):
    METRICS_FILE = Path(__file__).parent / "metrics" / "test_metrics.txt"
    
    def do_GET(self):
        """Handle GET requests"""
        if self.path == "/metrics":
            self.send_metrics()
        elif self.path == "/health":
            self.send_response(200)
            self.send_header("Content-type", "text/plain")
            self.end_headers()
            self.wfile.write(b"OK")
        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b"Not Found")
    
    def send_metrics(self):
        """Send metrics from test_metrics.txt"""
        try:
            if self.METRICS_FILE.exists():
                with open(self.METRICS_FILE, 'r') as f:
                    metrics_data = f.read()
            else:
                # Return empty metrics if file doesn't exist yet
                metrics_data = "# No metrics available yet\n"
            
            self.send_response(200)
            self.send_header("Content-type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(metrics_data)))
            self.end_headers()
            self.wfile.write(metrics_data.encode('utf-8'))
            
            # Log access
            print(f"[{datetime.now().strftime('%H:%M:%S')}] Served metrics - {len(metrics_data)} bytes")
        
        except Exception as e:
            print(f"[ERROR] {e}")
            self.send_response(500)
            self.end_headers()
            self.wfile.write(b"Internal Server Error")
    
    def log_message(self, format, *args):
        """Suppress default logging"""
        pass

def run_server(host="0.0.0.0", port=9091):
    """Start the metrics server"""
    server_address = (host, port)
    httpd = HTTPServer(server_address, MetricsHandler)
    
    print(f"[*] Metrics Server Started")
    print(f"[*] Listening on: http://{host}:{port}")
    print(f"[*] Metrics endpoint: http://localhost:{port}/metrics")
    print(f"[*] Health check: http://localhost:{port}/health")
    print(f"[*] Reading metrics from: {MetricsHandler.METRICS_FILE}")
    print(f"[*] Press Ctrl+C to stop\n")
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n[*] Server stopped")
        httpd.server_close()

if __name__ == "__main__":
    run_server()
