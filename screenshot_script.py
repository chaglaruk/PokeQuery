import os
import time
import subprocess
import xml.etree.ElementTree as ET

def run_cmd(cmd):
    return subprocess.check_output(cmd, shell=True).decode('utf-8')

def wait_for_device():
    print("Waiting for device...")
    while True:
        try:
            out = run_cmd("adb devices")
            if "device\n" in out or "device\r\n" in out:
                print("Device found!")
                break
        except:
            pass
        time.sleep(2)

def tap_node(node):
    bounds = node.attrib.get('bounds')
    if bounds:
        bounds = bounds.strip('[]').split('][')
        x1, y1 = map(int, bounds[0].split(','))
        x2, y2 = map(int, bounds[1].split(','))
        cx = (x1 + x2) // 2
        cy = (y1 + y2) // 2
        print(f"Tapping '{node.attrib.get('text', '')}' at {cx}, {cy}")
        os.system(f"adb shell input tap {cx} {cy}")
        time.sleep(2)
        return True
    return False

def find_and_tap(text_to_find, content_desc=False):
    os.system("adb shell uiautomator dump /sdcard/window_dump.xml > NUL 2>&1")
    os.system("adb pull /sdcard/window_dump.xml . > NUL 2>&1")
    try:
        tree = ET.parse("window_dump.xml")
        root = tree.getroot()
        for node in root.iter('node'):
            attr = 'content-desc' if content_desc else 'text'
            val = node.attrib.get(attr, '')
            if text_to_find in val:
                return tap_node(node)
    except:
        pass
    print(f"Could not find {text_to_find}")
    return False

def screencap(name):
    print(f"Capturing {name}...")
    os.system(f"adb shell screencap -p /sdcard/{name}.png")
    os.system(f"adb pull /sdcard/{name}.png docs/screenshots/{name}.png > NUL 2>&1")
    time.sleep(1)

def main():
    wait_for_device()
    print("Installing app...")
    os.system("adb install -r app/build/outputs/apk/debug/app-debug.apk")
    
    # Force clear data so we see Onboarding
    os.system("adb shell pm clear com.caglar.pokequery")
    os.system("adb shell monkey -p com.caglar.pokequery 1")
    time.sleep(3)
    
    # 1. Onboarding
    screencap("1_Onboarding")
    find_and_tap("Start building")
    
    # 2. Home
    time.sleep(1)
    screencap("2_Home")
    
    # 3. Guided Questions (Safe Cleanup)
    find_and_tap("Safe Cleanup")
    time.sleep(1)
    screencap("3_GuidedQuestions_SafeCleanup")
    
    # 4. Safe Cleanup Preview
    find_and_tap("Generate String")
    time.sleep(1)
    screencap("4_SafeCleanup_Preview")
    
    # Navigate Home
    find_and_tap("<- Back")
    time.sleep(1)
    find_and_tap("<- Back")
    time.sleep(1)
    
    # 5. 2x Candy Prep Preview
    find_and_tap("2x Candy Prep")
    find_and_tap("Generate String")
    time.sleep(1)
    screencap("5_CandyPrep_Preview")
    
    # Navigate Home
    find_and_tap("<- Back")
    time.sleep(1)
    find_and_tap("<- Back")
    time.sleep(1)
    
    # 6. Trade Fodder Preview
    find_and_tap("Trade Fodder")
    find_and_tap("Generate String")
    time.sleep(1)
    screencap("6_TradeFodder_Preview")
    
    # Navigate Home
    find_and_tap("<- Back")
    time.sleep(1)
    find_and_tap("<- Back")
    time.sleep(1)
    
    # 8. Expert Builder (Go to Builder Tab)
    # The bottom nav might not have text, maybe content-desc? 
    # Or just tap the coordinates, but let's try finding text "Builder"
    if not find_and_tap("Builder", content_desc=True):
        find_and_tap("Builder") # try text
    time.sleep(1)
    screencap("8_ExpertBuilder")
    
    # 9. Favorites
    if not find_and_tap("Favorites", content_desc=True):
        find_and_tap("Favorites")
    time.sleep(1)
    screencap("9_Favorites")
    
    # 10. Settings
    if not find_and_tap("Settings", content_desc=True):
        find_and_tap("Settings")
    time.sleep(1)
    screencap("10_Settings")
    
    # 7. Knowledge Base (Inside Settings)
    find_and_tap("View Dictionary")
    time.sleep(1)
    screencap("7_KnowledgeBase")
    
    print("Done capturing screenshots!")

if __name__ == '__main__':
    main()
