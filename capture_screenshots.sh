#!/bin/bash
mkdir -p docs/screenshots

# Wait for activity to be completely ready
function capture() {
    adb shell am start -n com.example.pokequery/.MainActivity --es start_route $1
    sleep 2
    adb shell screencap -p /sdcard/$2.png
    adb pull /sdcard/$2.png docs/screenshots/$2.png
}

capture "onboarding" "1_Onboarding"
capture "home" "2_Home"
capture "guided_safe_cleanup" "3_GuidedQuestions"
capture "preview_safe_cleanup" "4_SafeCleanup_Preview"
capture "preview_candy_prep" "5_CandyPrep_Preview"
capture "preview_trade_fodder" "6_TradeFodder_Preview"
capture "knowledge" "7_KnowledgeBase"
capture "expert" "8_ExpertBuilder"
capture "favorites" "9_Favorites"
capture "settings" "10_Settings"
