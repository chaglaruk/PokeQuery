New-Item -ItemType Directory -Force -Path "docs/screenshots" | Out-Null

function Capture-Screenshot {
    param (
        [string]$Route,
        [string]$Filename
    )
    Write-Host "Capturing $Filename..."
    adb shell am start -S -n com.caglar.pokequery/.MainActivity --es start_route $Route
    Start-Sleep -Seconds 2
    adb shell screencap -p /sdcard/${Filename}.png
    adb pull /sdcard/${Filename}.png docs/screenshots/${Filename}.png | Out-Null
}

Capture-Screenshot "onboarding" "1_Onboarding"
Capture-Screenshot "home" "2_Home"
Capture-Screenshot "detail_safe_cleanup" "3_Goal_SafeCleanup"
Capture-Screenshot "detail_candy_prep" "4_Goal_CandyPrep"
Capture-Screenshot "detail_pvp_candidates" "5_Goal_PvPCandidates"
Capture-Screenshot "presets" "6_Presets"
Capture-Screenshot "knowledge" "7_KnowledgeBase"
Capture-Screenshot "expert" "8_ExpertBuilder"
Capture-Screenshot "favorites" "9_Favorites"
Capture-Screenshot "settings" "10_Settings"
