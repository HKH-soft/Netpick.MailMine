# Fix ALL Java package declarations and imports

$javaPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform"

# Get all Java files
Get-ChildItem -Path $javaPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix package declarations
    $content = $content -replace "package ir\.netpick\.platform\.auth\.", "package ir.netpick.platform.gatekeeper."
    $content = $content -replace "package ir\.netpick\.platform\.common\.", "package ir.netpick.platform.core."
    $content = $content -replace "package ir\.netpick\.platform\.scrape\.", "package ir.netpick.platform.mailmine."
    
    # Fix imports
    $content = $content -replace "import ir\.netpick\.platform\.auth\.", "import ir.netpick.platform.gatekeeper."
    $content = $content -replace "import ir\.netpick\.platform\.common\.", "import ir.netpick.platform.core."
    $content = $content -replace "import ir\.netpick\.platform\.scrape\.", "import ir.netpick.platform.mailmine."
    $content = $content -replace "import static ir\.netpick\.mailmine\.auth\.", "import static ir.netpick.platform.gatekeeper."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "All Java packages fixed"