param(
    [string]$User = "root",
    [string]$Password = "",
    [string]$Database = "it_training_center_db",
    [string]$OutputDir = ".\backups"
)

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outputFile = Join-Path $OutputDir "$Database`_$timestamp.sql"

if ([string]::IsNullOrWhiteSpace($Password)) {
    mysqldump -u $User $Database | Out-File -FilePath $outputFile -Encoding utf8
} else {
    mysqldump -u $User "-p$Password" $Database | Out-File -FilePath $outputFile -Encoding utf8
}

Write-Host "Backup saved to $outputFile"
