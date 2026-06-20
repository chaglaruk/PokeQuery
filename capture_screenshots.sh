#!/bin/bash
mkdir -p docs/screenshots

# Wait for activity to be completely ready
function capture() {
    adb shell am start -S -n com.caglar.pokequery/.MainActivity --es start_route $1
    sleep 2
    adb shell screencap -p /sdcard/$2.png
    adb pull /sdcard/$2.png docs/screenshots/$2.png
}

capture "onboarding" "1_Onboarding"
capture "home" "2_Home"
capture "detail_safe_cleanup" "3_Goal_SafeCleanup"
capture "detail_candy_prep" "4_Goal_CandyPrep"
capture "detail_pvp_candidates" "5_Goal_PvPCandidates"
capture "presets" "6_Presets"
capture "knowledge" "7_KnowledgeBase"
capture "expert" "8_ExpertBuilder"
capture "favorites" "9_Favorites"
capture "settings" "10_Settings"
