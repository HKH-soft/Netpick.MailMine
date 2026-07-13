# Update frontend API paths for Netpick rebrand

$frontendSrc = "H:\Netpick.MailMine\Frontend\src"

# API path mappings
$apiMappings = @{
    "/api/v1/auth/" = "/api/v1/gatekeeper/auth/"
    "/api/v1/users" = "/api/v1/gatekeeper/users"
    "/api/v1/admin" = "/api/v1/gatekeeper/admin"
    "/api/v1/scrape/" = "/api/v1/mailmine/scrape/"
    "/api/v1/pipelines" = "/api/v1/mailmine/pipelines"
    "/api/v1/contacts" = "/api/v1/mailmine/contacts"
    "/api/v1/search_queries" = "/api/v1/mailmine/search-queries"
    "/api/v1/api_keys" = "/api/v1/mailmine/api-keys"
    "/api/v1/scrape_data" = "/api/v1/mailmine/scrape-data"
    "/api/v1/scrape_jobs" = "/api/v1/mailmine/scrape-jobs"
    "/api/v1/proxies" = "/api/v1/mailmine/proxies"
    "/api/v1/email" = "/api/v1/mailmine/email"
    "/api/v1/campaigns" = "/api/v1/mailmine/campaigns"
    "/api/v1/email-templates" = "/api/v1/mailmine/email-templates"
    "/api/v1/email-messages" = "/api/v1/mailmine/email-messages"
    "/api/v1/email-auth" = "/api/v1/mailmine/email-auth"
    "/api/v1/email-queue" = "/api/v1/mailmine/email-queue"
    "/api/v1/email-tags" = "/api/v1/mailmine/email-tags"
    "/api/v1/attachments" = "/api/v1/mailmine/attachments"
    "/api/v1/follow-ups" = "/api/v1/mailmine/follow-ups"
    "/api/v1/analytics" = "/api/v1/mailmine/analytics"
    "/api/v1/segments" = "/api/v1/mailmine/segments"
    "/api/v1/email-rules" = "/api/v1/mailmine/email-rules"
    "/api/v1/shared-inboxes" = "/api/v1/mailmine/shared-inboxes"
    "/api/v1/notifications" = "/api/v1/core/notifications"
    "/api/v1/gdpr" = "/api/v1/core/gdpr"
    "/api/v1/search" = "/api/v1/core/search"
    "/api/v1/spam-detection" = "/api/v1/core/spam-detection"
}

# Process all TypeScript/JavaScript files
Get-ChildItem -Path $frontendSrc -Recurse -File -Include "*.ts","*.tsx","*.js" | Where-Object { Test-Path $_.FullName } | ForEach-Object {
    $file = $_
    $content = Get-Content $file.FullName -Raw
    
    foreach ($mapping in $apiMappings.GetEnumerator()) {
        $content = $content -replace [regex]::Escape($mapping.Key), $mapping.Value
    }
    
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Frontend API paths updated"