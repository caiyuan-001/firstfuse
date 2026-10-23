# ============================================================
# 创融通 PostgreSQL 备份脚本（M4）
# 用法: powershell -ExecutionPolicy Bypass -File scripts\backup-pg.ps1 [-KeepDays 14]
# 产物: apps/server/backups/firstfuse-yyyyMMdd-HHmmss.dump（pg_dump 自定义格式，可 pg_restore）
# 依赖: 本机需有 pg_dump（PostgreSQL 客户端工具，版本 >= 服务端大版本）
# ============================================================
param(
    [int]$KeepDays = 14
)

$ErrorActionPreference = 'Stop'
$serverRoot = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $serverRoot '.env'
$backupDir = Join-Path $serverRoot 'backups'

if (-not (Test-Path $envFile)) {
    Write-Host "[ERROR] 未找到 $envFile" -ForegroundColor Red
    exit 1
}

# 读取 .env 中的 PG 连接信息
$cfg = @{}
Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { return }
    $idx = $line.IndexOf('=')
    if ($idx -lt 1) { return }
    $cfg[$line.Substring(0, $idx).Trim()] = $line.Substring($idx + 1).Trim()
}

# 定位 pg_dump
$pgDump = Get-Command pg_dump -ErrorAction SilentlyContinue
if (-not $pgDump) {
    $candidates = @(
        'C:\Program Files\PostgreSQL\17\bin\pg_dump.exe',
        'C:\Program Files\PostgreSQL\16\bin\pg_dump.exe',
        'C:\Program Files\PostgreSQL\15\bin\pg_dump.exe',
        'D:\PostgreSQL\17\bin\pg_dump.exe',
        'D:\PostgreSQL\16\bin\pg_dump.exe'
    ) | Where-Object { Test-Path $_ }
    if (@($candidates).Count -eq 0) {
        Write-Host "[ERROR] 未找到 pg_dump，请安装 PostgreSQL 客户端工具或将其 bin 加入 PATH" -ForegroundColor Red
        exit 1
    }
    $pgDump = @($candidates)[0]
} else {
    $pgDump = $pgDump.Source
}

if (-not (Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$outFile = Join-Path $backupDir "firstfuse-$stamp.dump"

$env:PGPASSWORD = $cfg['PG_PASSWORD']
Write-Host "[OK] 开始备份 $($cfg['PG_HOST']):$($cfg['PG_PORT'])/$($cfg['PG_DB']) ..."
& $pgDump --host=$($cfg['PG_HOST']) --port=$($cfg['PG_PORT']) --username=$($cfg['PG_USERNAME']) `
    --format=custom --compress=6 --file="$outFile" $($cfg['PG_DB'])
$code = $LASTEXITCODE
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

if ($code -ne 0) {
    Write-Host "[ERROR] pg_dump 失败（exit $code）" -ForegroundColor Red
    exit $code
}
$size = (Get-Item $outFile).Length
Write-Host ("[OK] 备份完成：{0}（{1:N0} 字节）" -f $outFile, $size) -ForegroundColor Green

# 保留策略：删除 KeepDays 天前的旧备份
$cutoff = (Get-Date).AddDays(-$KeepDays)
$removed = 0
Get-ChildItem $backupDir -Filter 'firstfuse-*.dump' | Where-Object { $_.LastWriteTime -lt $cutoff } | ForEach-Object {
    Remove-Item $_.FullName -Force
    $removed++
}
if ($removed -gt 0) {
    Write-Host "[OK] 清理 $removed 个 $KeepDays 天前的旧备份" -ForegroundColor Green
}

# 恢复示例：
#   pg_restore --host=... --port=... --username=... --clean --dbname=firstfuse <dump文件>
