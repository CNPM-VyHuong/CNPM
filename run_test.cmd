@echo off
REM Run tests for a single service and update Grafana dashboard
REM Usage: run_test.cmd <service_name>
REM Example: run_test.cmd user_service

setlocal enabledelayedexpansion

set "BACKEND_PATH=D:\cnpm\CNPM-3\DoAnCNPM_Backend"
set "SCRIPT_PATH=D:\cnpm\CNPM-3\scripts\run_single_service_test.py"

if "%1"=="" (
    echo Usage: run_test.cmd service_name
    echo.
    echo Available services:
    echo   - user_service
    echo   - product_service
    echo   - drone_service
    echo   - order_service
    echo   - payment_service
    echo   - restaurant-service
    echo.
    echo Examples:
    echo   run_test.cmd user_service
    echo   run_test.cmd product_service
    exit /b 1
)

echo.
echo ========================================
echo Running tests for: %1
echo ========================================
echo.

python "%SCRIPT_PATH%" "%BACKEND_PATH%" %1

if errorlevel 1 (
    echo.
    echo Tests FAILED - Check console output above
    exit /b 1
) else (
    echo.
    echo Tests PASSED - Grafana dashboard updating...
    echo Dashboard: http://localhost:3001
    exit /b 0
)
