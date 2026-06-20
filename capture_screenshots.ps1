New-Item -ItemType Directory -Force -Path "docs/screenshots" | Out-Null
Remove-Item -Force docs\screenshots\*.png -ErrorAction SilentlyContinue

function Capture-Screenshot {
    param (
        [string]$Route,
        [string]$Filename
    )
    Write-Host "Capturing $Filename..."
    adb shell am start -S -n com.caglar.pokequery/.MainActivity --es start_route $Route
    Start-Sleep -Seconds 2
    adb shell screencap -p /sdcard/${Filename}
    adb pull /sdcard/${Filename} docs/screenshots/${Filename} | Out-Null
}

Capture-Screenshot "onboarding" "1_onboarding_step_1.png"
Capture-Screenshot "onboarding_step_2" "2_onboarding_step_2.png"
Capture-Screenshot "onboarding_step_3" "3_onboarding_step_3.png"
Capture-Screenshot "home" "4_home.png"
Capture-Screenshot "detail_safe_cleanup" "5_safe_cleanup_detail.png"
Capture-Screenshot "detail_candy_prep" "6_candy_prep_detail.png"
Capture-Screenshot "detail_trade_fodder" "7_trade_fodder_detail.png"
Capture-Screenshot "detail_nundo_finder" "8_nundo_detail.png"
Capture-Screenshot "detail_pvp_candidates" "9_pvp_detail.png"
Capture-Screenshot "detail_lucky_trade" "10_lucky_trade_detail.png"
Capture-Screenshot "presets" "11_popular_presets.png"
Capture-Screenshot "knowledge" "12_knowledge_search.png"
Capture-Screenshot "knowledge_expanded" "13_knowledge_expanded.png"
Capture-Screenshot "favorites" "14_favorites.png"
Capture-Screenshot "history" "15_history.png"
Capture-Screenshot "settings" "16_settings.png"
