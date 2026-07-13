# Package Restructuring Script for Netpick Rebrand
# Moves ir.netpick.mailmine -> ir.netpick.platform with gatekeeper/mailmine/core subpackages

$ErrorActionPreference = "Stop"

$srcRoot = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\mailmine"
$dstRoot = "H:\Netpick.MailMine\Backend\src\main\java\ir\netpick\platform"

# Define package mappings
$packageMappings = @{
    "auth" = "gatekeeper"
    "scrape" = "mailmine"
    "email" = "mailmine"
    "ai" = "core"
    "common" = "core"
    "config" = "core"
    "init" = "core"
}

function Update-PackageStatement {
    param([string]$content, [string]$oldPackage, [string]$newPackage)
    
    # Update package declaration
    $content = $content -replace "package $oldPackage\.", "package $newPackage."
    
    # Update imports
    $content = $content -replace "import $oldPackage\.", "import $newPackage."
    
    return $content
}

function Move-Package {
    param([string]$srcSubPackage, [string]$dstSubPackage)
    
    $srcPath = Join-Path $srcRoot $srcSubPackage
    $dstPath = Join-Path $dstRoot $dstSubPackage
    
    if (-not (Test-Path $srcPath)) {
        Write-Host "Source path not found: $srcPath"
        return
    }
    
    # Create destination directory
    New-Item -ItemType Directory -Force -Path $dstPath | Out-Null
    
    # Get all Java files
    $javaFiles = Get-ChildItem -Path $srcPath -Recurse -File -Filter "*.java"
    
    foreach ($file in $javaFiles) {
        $relativePath = $file.FullName.Substring($srcPath.Length + 1)
        $destFile = Join-Path $dstPath $relativePath
        
        # Create destination subdirectory if needed
        $destDir = Split-Path $destFile -Parent
        if (-not (Test-Path $destDir)) {
            New-Item -ItemType Directory -Force -Path $destDir | Out-Null
        }
        
        # Read and update content
        $content = Get-Content $file.FullName -Raw
        
        # Replace package and import statements
        $content = $content -replace "package ir\.netpick\.mailmine\.", "package ir.netpick.platform."
        $content = $content -replace "import ir\.netpick\.mailmine\.", "import ir.netpick.platform."
        
        # Write to new location
        Set-Content -Path $destFile -Value $content -Encoding UTF8
        Write-Host "Moved: $relativePath"
    }
}

# Process each package mapping
foreach ($mapping in $packageMappings.GetEnumerator()) {
    Move-Package -srcSubPackage $mapping.Key -dstSubPackage $mapping.Value
}

Write-Host "Package restructuring complete. Now run mvn clean compile to verify."