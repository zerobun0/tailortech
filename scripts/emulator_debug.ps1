param(
    [string]$AvdName = "Medium_Phone_API_36.1",
    [string]$EmulatorSerial = "emulator-5554"
)

$ErrorActionPreference = "Stop"

$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$adb = Join-Path $sdk "platform-tools\adb.exe"
$emulator = Join-Path $sdk "emulator\emulator.exe"

if (-not (Test-Path $adb)) { throw "adb not found at $adb" }
if (-not (Test-Path $emulator)) { throw "emulator not found at $emulator" }

Write-Host "==> Starting emulator: $AvdName"
Start-Process -FilePath $emulator -ArgumentList @("-avd", $AvdName, "-no-boot-anim", "-no-snapshot-load") | Out-Null

Write-Host "==> Waiting for emulator boot"
for ($i = 0; $i -lt 120; $i++) {
    $state = (& $adb -s $EmulatorSerial get-state 2>$null)
    if ($state -eq "device") {
        $boot = (& $adb -s $EmulatorSerial shell getprop sys.boot_completed).Trim()
        if ($boot -eq "1") {
            break
        }
    }
    Start-Sleep -Seconds 2
}

$booted = (& $adb -s $EmulatorSerial shell getprop sys.boot_completed).Trim()
if ($booted -ne "1") {
    throw "Emulator $EmulatorSerial did not finish booting in time"
}

Write-Host "==> Building app and tests"
& .\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest

Write-Host "==> Installing APKs to emulator only"
& $adb -s $EmulatorSerial install -r "app/build/outputs/apk/debug/app-debug.apk"
& $adb -s $EmulatorSerial install -r "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"

Write-Host "==> Running instrumentation tests on emulator only"
& $adb -s $EmulatorSerial shell am instrument -w com.tailortech.app.test/androidx.test.runner.AndroidJUnitRunner

Write-Host "==> Launching app"
& $adb -s $EmulatorSerial shell am start -n "com.tailortech.app/.MainActivity" | Out-Null

Write-Host "==> Tail recent app logs"
& $adb -s $EmulatorSerial logcat -d | Select-String -Pattern "tailortech|AndroidRuntime|FATAL EXCEPTION" | Select-Object -Last 80

Write-Host "==> Done"
