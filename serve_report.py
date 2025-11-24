#!/usr/bin/env python3
"""
Simple HTTP server to serve test report
"""

import http.server
import socketserver
import os
from pathlib import Path

PORT = 8000
REPORT_DIR = Path(__file__).parent

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(REPORT_DIR), **kwargs)
    
    def log_message(self, format, *args):
        print(f"[{self.log_date_time_string()}] {format % args}")

if __name__ == "__main__":
    handler = MyHTTPRequestHandler
    
    with socketserver.TCPServer(("", PORT), handler) as httpd:
        print(f"🌐 Test Report Server running at http://localhost:{PORT}/test-report.html")
        print(f"📁 Serving files from: {REPORT_DIR}")
        print("Press Ctrl+C to stop...")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n✅ Server stopped")
