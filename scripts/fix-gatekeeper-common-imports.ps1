# Fix Java imports in gatekeeper directory that reference common packages

$gatekeeperPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform\gatekeeper"

# Get all Java files in gatekeeper
Get-ChildItem -Path $gatekeeperPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix imports
    $content = $content -replace "import ir\.netpick\.platform\.common\.", "import ir.netpick.platform.core."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Gatekeeper common imports fixed"