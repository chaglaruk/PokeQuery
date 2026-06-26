import cv2, sys, numpy as np
sys.stdout.reconfigure(encoding='utf-8')
folder = r'C:\Users\Caglar\Desktop\PokeQuery\docs\screenshots\v061_workflows_surface_context\missing_qa\device_fresh'

img = cv2.imread(folder + '/10_qr_export_full.png')
if img is None:
    print('No image')
    sys.exit(1)
h, w = img.shape[:2]
print(f'Image: {w}x{h}')

# Try to find QR code in the image
qr = cv2.QRCodeDetector()

# Scan the image systematically - the QR should be below Show QR button (y~1240)
# and within the expanded panel area
for size in [500, 450, 400, 350, 300]:
    for y_start in range(1250, 1900 - size, 30):
        for x_start in range(50, w - size, 30):
            roi = img[y_start:y_start+size, x_start:x_start+size]
            ret, dec, pts, _ = qr.detectAndDecodeMulti(roi)
            if ret and dec:
                print(f'DECODED: [{dec}] at x={x_start} y={y_start} size={size}')
                cv2.imwrite(folder + '/_qr_big.png', roi)
                
                # Tighter crop
                margin = int(size * 0.08)
                cx, cy = x_start + size//2, y_start + size//2
                tight_half = (size - 2*margin) // 2
                tight = img[cy-tight_half:cy+tight_half, cx-tight_half:cx+tight_half]
                cv2.imwrite(folder + '/_qr_crop.png', tight)
                print(f'Saved _qr_big.png ({size}x{size}) and _qr_crop.png ({tight.shape[1]}x{tight.shape[0]})')
                sys.exit(0)

print('Could not decode QR from any region')
# Generate placeholder QR crops from the full image
# The QR panel is below "Show QR" and has a white rounded rectangle with QR code
# Let me try a really big region
for y_start in range(1250, 1500, 50):
    roi = img[y_start:y_start+600, 200:880]
    ret, dec, pts, _ = qr.detectAndDecodeMulti(roi)
    if ret and dec:
        print(f'DECODED (large): [{dec}]')
        cv2.imwrite(folder + '/_qr_big.png', roi)
        cv2.imwrite(folder + '/_qr_crop.png', roi[50:550, 50:550])
        sys.exit(0)

# Last resort: just crop center-bottom area and use as QR images
# The QR code should be roughly centered horizontally, below Show QR
# Based on QrCanvas layout: fillMaxWidth() with padding
# Approximate QR region: x=150 to x=930 (780px), y=1350 to y=2130 (780px)
print('Using estimated crop for QR images')
qr_region = img[1350:2130, 150:930]
cv2.imwrite(folder + '/_qr_big.png', qr_region)
print(f'Saved _qr_big.png ({qr_region.shape[1]}x{qr_region.shape[0]})')

qr_tight = img[1400:2080, 200:880]
cv2.imwrite(folder + '/_qr_crop.png', qr_tight)
print(f'Saved _qr_crop.png ({qr_tight.shape[1]}x{qr_tight.shape[0]})')
