# Fix remaining Java imports referencing common packages

$javaPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform"

# Get all Java files
Get-ChildItem -Path $javaPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix imports
    $content = $content -replace "import ir\.netpick\.platform\.common\.", "import ir.netpick.platform.core."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Remaining common imports fixed"