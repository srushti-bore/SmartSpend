@echo off
setlocal
echo ===================================================
echo     SmartSpend 1-Click ADB Installer & Launcher
echo ===================================================
echo.

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist %ADB% (
    set ADB=adb
)

echo [1/3] Checking connected Android devices...
%ADB% devices
echo.

echo [2/3] Installing SmartSpend Debug APK...
%ADB% install -r -t -g "%~dp0app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Installation failed. Please ensure your phone is unlocked and USB Debugging is authorized.
    pause
    exit /b %ERRORLEVEL%
)
echo.

echo [3/3] Launching SmartSpend on device...
%ADB% shell am start -n com.smartspend.app/.MainActivity
echo.
echo ===================================================
echo   SmartSpend successfully installed and launched!
echo ===================================================
pause
