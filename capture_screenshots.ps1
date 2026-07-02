$OutputDir = "docs/screenshots/v069_final_user_qa_polish"
$Device = $env:ANDROID_SERIAL
if ([string]::IsNullOrWhiteSpace($Device)) {
    $deviceLine = adb devices | Select-String "`tdevice$" | Select-Object -First 1
    if ($deviceLine) {
        $Device = $deviceLine.ToString().Split("`t")[0]
    }
}
if ([string]::IsNullOrWhiteSpace($Device)) {
    throw "No connected adb device found."
}
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

function Capture-Screenshot {
    param (
        [string]$Route,
        [string]$Filename,
        [string]$AppLanguage = "",
        [string]$SearchLanguage = "",
        [int]$TapX = 0,
        [int]$TapY = 0,
        [int]$PreTapScrolls = 0
    )
    Write-Host "Capturing $Filename..."
    $adbArgs = @("-s", $Device, "shell", "am", "start", "-S", "-n", "com.caglar.pokequery/.MainActivity", "--es", "start_route", $Route)
    if ($AppLanguage -ne "") { $adbArgs += @("--es", "app_language", $AppLanguage) }
    if ($SearchLanguage -ne "") { $adbArgs += @("--es", "search_language", $SearchLanguage) }
    adb @adbArgs | Out-Null
    Start-Sleep -Seconds 2
    for ($i = 0; $i -lt $PreTapScrolls; $i++) {
        adb -s $Device shell input swipe 540 2000 540 760 500
        Start-Sleep -Milliseconds 700
    }
    if ($TapX -gt 0 -and $TapY -gt 0) {
        adb -s $Device shell input tap $TapX $TapY
        Start-Sleep -Seconds 1
    }
    adb -s $Device shell screencap -p /sdcard/$Filename
    adb -s $Device pull /sdcard/$Filename "$OutputDir/$Filename" | Out-Null
}

Capture-Screenshot "onboarding" "onboarding_step_1_en.png" "en"
Capture-Screenshot "onboarding_step_2" "onboarding_step_2_en.png" "en"
Capture-Screenshot "onboarding" "onboarding_step_1_tr.png" "tr"
Capture-Screenshot "onboarding_step_2" "onboarding_step_2_tr.png" "tr"
Capture-Screenshot "home" "home_tr.png" "tr"
Capture-Screenshot "detail_safe_cleanup" "detail_or_risk_tr.png" "tr"
Capture-Screenshot "assistant" "search_assistant_en.png" "en"
Capture-Screenshot "assistant" "search_assistant_tr.png" "tr"
Capture-Screenshot "events" "event_guide_en_before_refresh.png" "en"
Capture-Screenshot "events" "event_guide_en_after_refresh.png" "en" "" 960 110
Capture-Screenshot "events" "event_guide_tr_before_refresh.png" "tr"
Capture-Screenshot "events" "event_guide_tr_after_refresh.png" "tr" "" 960 110
Capture-Screenshot "events" "event_guide_scrolled_or_expanded_en.png" "en" "" 0 0 1
Capture-Screenshot "events" "event_guide_scrolled_or_expanded_tr.png" "tr" "" 0 0 1
Capture-Screenshot "settings" "settings_en.png" "en"
Capture-Screenshot "settings" "settings_tr.png" "tr"
