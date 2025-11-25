@echo off
REM Start Metrics Server on port 9091
REM This serves test metrics to Prometheus

setlocal enabledelayedexpansion

echo.
echo ======================================
echo Starting Metrics Server
echo ======================================
echo.
echo Endpoint: http://localhost:9091/metrics
echo Health:   http://localhost:9091/health
echo.

python.exe -u monitoring/metrics-server.py

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start metrics server
    pause
    exit /b 1
)
