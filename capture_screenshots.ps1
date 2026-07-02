$OutputDir = "docs/screenshots/v068_codex_recovery_pass1"
$Device = "192.168.1.126:5555"
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
Remove-Item -Force "$OutputDir\*.png" -ErrorAction SilentlyContinue

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

function Capture-LauncherIconPreview {
    $filename = "launcher_icon_preview_or_launcher_screenshot.png"
    Write-Host "Capturing $filename..."
    $out = (Join-Path $OutputDir $filename).Replace("\", "/")
    $code = "from pathlib import Path; from PIL import Image, ImageDraw; src=Path(r'app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp'); out=Path(r'$out'); bg=Image.new('RGB',(1080,2400),(8,13,28)); draw=ImageDraw.Draw(bg); img=Image.open(src).convert('RGBA').resize((720,720)); bg.paste(img,((1080-img.width)//2,520),img); draw.text((96,1320),'Launcher icon preview',fill=(230,240,255)); draw.text((96,1380),'Generated from app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp',fill=(165,178,197)); bg.save(out)"
    python -c $code
}

Capture-Screenshot "onboarding" "onboarding_step_1_en.png" "en"
Capture-Screenshot "onboarding_step_2" "onboarding_step_2_en.png" "en"
Capture-Screenshot "onboarding" "onboarding_step_1_tr.png" "tr"
Capture-Screenshot "onboarding_step_2" "onboarding_step_2_tr.png" "tr"

Capture-Screenshot "home" "home_en.png" "en"
Capture-Screenshot "home" "home_tr.png" "tr"
Capture-Screenshot "home" "home_de.png" "de"
Capture-Screenshot "home" "home_es.png" "es"
Capture-Screenshot "home" "home_fr.png" "fr"
Capture-Screenshot "home" "home_it.png" "it"
Capture-Screenshot "home" "home_chip_info_open_en.png" "en" "" 360 640
Capture-Screenshot "home" "home_chip_info_open_tr.png" "tr" "" 360 640

Capture-Screenshot "settings" "settings_en.png" "en"
Capture-Screenshot "settings" "settings_tr.png" "tr"
Capture-Screenshot "settings" "settings_de.png" "de"
Capture-Screenshot "settings" "app_language_picker_open.png" "tr" "" 540 520 1
Capture-Screenshot "settings" "search_string_language_picker_open.png" "tr" "" 540 960 1
Capture-Screenshot "settings" "token_language_warning_tr.png" "tr" "" 0 0 1

Capture-Screenshot "settings" "event_guide_settings_en.png" "en"
Capture-Screenshot "settings" "event_guide_settings_tr.png" "tr"
Capture-Screenshot "events" "event_guide_before_refresh_en.png" "en"
Capture-Screenshot "events" "event_guide_after_refresh_en.png" "en" "" 540 360
Capture-Screenshot "events" "event_guide_before_refresh_tr.png" "tr"
Capture-Screenshot "events" "event_guide_after_refresh_tr.png" "tr" "" 540 360
Capture-Screenshot "events" "event_guide_offline_or_fallback_state_if_applicable.png" "en"

Capture-Screenshot "assistant" "search_assistant_tr.png" "tr"
Capture-Screenshot "detail_safe_cleanup" "safe_cleanup_detail_en.png" "en"
Capture-Screenshot "detail_pvp_candidates" "pvp_detail_en.png" "en"
Capture-Screenshot "detail_nundo_finder" "nundo_detail_en.png" "en"
Capture-Screenshot "detail_lucky_trade" "lucky_trade_detail_en.png" "en"
Capture-Screenshot "detail_safe_cleanup" "generated_safe_cleanup_english_tokens.png" "en" "en"
Capture-Screenshot "detail_safe_cleanup" "generated_safe_cleanup_turkish_tokens_if_verified.png" "tr" "tr"
Capture-Screenshot "detail_pvp_candidates" "generated_pvp_english_tokens.png" "en" "en"
Capture-Screenshot "detail_pvp_candidates" "generated_pvp_turkish_tokens_if_verified.png" "tr" "tr"
Capture-LauncherIconPreview
