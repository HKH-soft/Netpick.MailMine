# Fix Java package declarations and imports in gatekeeper directory

$gatekeeperPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform\gatekeeper"

# Get all Java files in gatekeeper
Get-ChildItem -Path $gatekeeperPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix package declarations
    $content = $content -replace "package ir\.netpick\.platform\.auth\.", "package ir.netpick.platform.gatekeeper."
    $content = $content -replace "package ir\.netpick\.platform\.common\.", "package ir.netpick.platform.core."
    
    # Fix imports
    $content = $content -replace "import ir\.netpick\.platform\.auth\.", "import ir.netpick.platform.gatekeeper."
    $content = $content -replace "import ir\.netpick\.platform\.common\.", "import ir.netpick.platform.core."
    $content = $content -replace "import static ir\.netpick\.mailmine\.auth\.", "import static ir.netpick.platform.gatekeeper."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Java packages fixed"