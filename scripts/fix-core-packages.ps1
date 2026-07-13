# Fix Java packages in core directory

$corePath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform\core"

# Get all Java files in core
Get-ChildItem -Path $corePath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix package declarations
    $content = $content -replace "package ir\.netpick\.platform\.common\.", "package ir.netpick.platform.core."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Core packages fixed"