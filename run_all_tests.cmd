@echo off
REM Run tests for all services sequentially
REM Updates Grafana dashboard in real-time as each service finishes
REM Usage: run_all_tests.cmd

setlocal enabledelayedexpansion

set "BACKEND_PATH=D:\cnpm\CNPM-3\DoAnCNPM_Backend"
set "SCRIPT_PATH=D:\cnpm\CNPM-3\scripts\run_all_services_test.py"

echo.
echo ========================================
echo Running tests for ALL SERVICES
echo ========================================
echo.
echo This will run tests sequentially and update Grafana dashboard
echo Dashboard: http://localhost:3001
echo.

python "%SCRIPT_PATH%" "%BACKEND_PATH%"

if errorlevel 1 (
    echo.
    echo Some tests FAILED - Check results above
    exit /b 1
) else (
    echo.
    echo All tests PASSED
    exit /b 0
)
