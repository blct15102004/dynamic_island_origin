# IsleBridge Keystore Generation & Base64 Export Script
# Usage: Open PowerShell in project root and run: .\scripts\generate_keystore.ps1

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   IsleBridge - Trợ lý Tạo Khóa Ký (Release Keystore)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$keystoreFile = "islebridge-release.jks"
$alias = "islebridge_release"

if (Test-Path $keystoreFile) {
    Write-Host "[!] Đã tìm thấy file $keystoreFile trong thư mục!" -ForegroundColor Yellow
} else {
    $password = Read-Host "Nhập mật khẩu cho Keystore (tối thiểu 6 ký tự)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
    $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

    if ($plainPassword.Length -lt 6) {
        Write-Host "[-] Mật khẩu phải có ít nhất 6 ký tự!" -ForegroundColor Red
        exit 1
    }

    Write-Host "[+] Đang khởi tạo file keystore $keystoreFile..." -ForegroundColor Green
    
    # Check if keytool is available
    $keytoolPath = "keytool"
    try {
        & $keytoolPath -help | Out-Null
    } catch {
        # Check Minecraft JRE or other JDK
        $fallbackKeytool = "C:\Apps\App data\LegacyLauncher\Minecraft\jre\java-runtime-gamma\windows-x64\java-runtime-gamma\bin\keytool.exe"
        if (Test-Path $fallbackKeytool) {
            $keytoolPath = $fallbackKeytool
        } else {
            Write-Host "[-] Không tìm thấy công cụ 'keytool'. Vui lòng cài đặt JDK hoặc Android Studio." -ForegroundColor Red
            exit 1
        }
    }

    & $keytoolPath -genkeypair -v `
        -keystore $keystoreFile `
        -alias $alias `
        -keyalg RSA `
        -keysize 2048 `
        -validity 10000 `
        -storepass $plainPassword `
        -keypass $plainPassword `
        -dname "CN=IsleBridge, OU=Android, O=IsleBridge, L=Hanoi, C=VN"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[-] Lỗi khi tạo keystore!" -ForegroundColor Red
        exit 1
    }

    Write-Host "[✓] Đã tạo thành công: $keystoreFile" -ForegroundColor Green
    Write-Host "    - Key Alias: $alias" -ForegroundColor Gray
    Write-Host "    - Mật khẩu: (Đã lưu theo mật khẩu bạn vừa nhập)" -ForegroundColor Gray
}

# Encode to Base64
if (Test-Path $keystoreFile) {
    $bytes = [System.IO.File]::ReadAllBytes($keystoreFile)
    $base64 = [Convert]::ToBase64String($bytes)
    Set-Clipboard -Value $base64

    Write-Host "`n==========================================================" -ForegroundColor Green
    Write-Host "[✓] ĐÃ SAO CHÉP CHUỖI BASE64 VÀO CLIPBOARD CỦA BẠN!" -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "Bây giờ bạn chỉ cần vào GitHub:" -ForegroundColor Yellow
    Write-Host "1. Vào: https://github.com/blct15102004/dynamic_island_origin/settings/secrets/actions" -ForegroundColor White
    Write-Host "2. Nhấn 'New repository secret' và thêm 4 secret sau:" -ForegroundColor White
    Write-Host "   - ANDROID_KEYSTORE_BASE64   -> Dán (Ctrl+V) chuỗi vừa copy" -ForegroundColor Cyan
    Write-Host "   - ANDROID_KEYSTORE_PASSWORD -> Nhập mật khẩu bạn vừa tạo" -ForegroundColor Cyan
    Write-Host "   - ANDROID_KEY_ALIAS         -> islebridge_release" -ForegroundColor Cyan
    Write-Host "   - ANDROID_KEY_PASSWORD      -> Nhập mật khẩu bạn vừa tạo" -ForegroundColor Cyan
    Write-Host "==========================================================`n" -ForegroundColor Green
}
