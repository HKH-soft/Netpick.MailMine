# Fix ALL Java package declarations

$javaPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform"

# Get all Java files
Get-ChildItem -Path $javaPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix package declarations
    $content = $content -replace "package ir\.netpick\.platform\.auth\.", "package ir.netpick.platform.gatekeeper."
    $content = $content -replace "package ir\.netpick\.platform\.common\.", "package ir.netpick.platform.core."
    $content = $content -replace "package ir\.netpick\.platform\.email\.", "package ir.netpick.platform.mailmine."
    $content = $content -replace "package ir\.netpick\.platform\.scrape\.", "package ir.netpick.platform.mailmine."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "All package declarations fixed"