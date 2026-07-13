# Fix Java imports referencing email/scrape packages

$javaPath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform"

# Get all Java files
Get-ChildItem -Path $javaPath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix imports
    $content = $content -replace "import ir\.netpick\.platform\.email\.", "import ir.netpick.platform.mailmine."
    $content = $content -replace "import ir\.netpick\.platform\.scrape\.", "import ir.netpick.platform.mailmine."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Email/scrape imports fixed"