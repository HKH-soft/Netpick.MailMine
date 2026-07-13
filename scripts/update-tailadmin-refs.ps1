# Update TailAdmin references to Netpick in frontend

$frontendSrc = "H:\Netpick.MailMine\Frontend\src"

# Get all TypeScript/TSX files
Get-ChildItem -Path $frontendSrc -Recurse -File -Include "*.ts","*.tsx" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    # Replace TailAdmin references
    $content = $content -replace "TailAdmin", "Netpick"
    $content = $content -replace "TailAdmin - Next\.js Dashboard Template", "Netpick"
    $content = $content -replace "TailAdmin - Next\.js Tailwind CSS Admin Dashboard Template", "Netpick"
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "TailAdmin references updated"