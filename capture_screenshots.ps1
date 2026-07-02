$OutputDir = "docs/screenshots/v069_onboarding_event_guide"
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
Capture-Screenshot "settings" "settings_en.png" "en"
Capture-Screenshot "events" "event_guide_before_refresh_en.png" "en"
Capture-Screenshot "events" "event_guide_after_refresh_en.png" "en" "" 540 360
Capture-Screenshot "events" "event_guide_before_refresh_tr.png" "tr"
Capture-Screenshot "events" "event_guide_after_refresh_tr.png" "tr" "" 540 360
Capture-Screenshot "events" "event_detail_or_expanded_card_if_available.png" "en" "" 0 0 1

Capture-Screenshot "assistant" "search_assistant_tr.png" "tr"
