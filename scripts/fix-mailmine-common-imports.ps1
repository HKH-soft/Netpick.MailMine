# Fix Java imports in mailmine directory that reference common packages

$mailminePath = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform\mailmine"

# Get all Java files in mailmine
Get-ChildItem -Path $mailminePath -Recurse -File -Include "*.java" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Fix imports
    $content = $content -replace "import ir\.netpick\.platform\.common\.", "import ir.netpick.platform.core."
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Mailmine common imports fixed"