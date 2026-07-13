# Fix AI package declarations

$javaPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform"

# Get all Java files
Get-ChildItem -Path $javaPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix package declarations
    $content = $content -replace "package ir\.netpick\.platform\.ai\.", "package ir.netpick.platform.mailmine."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "AI packages fixed"