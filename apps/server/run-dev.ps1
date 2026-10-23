# 创融通后端启动脚本：读取同目录 .env 注入环境变量后启动 Spring Boot
# 用法: powershell -ExecutionPolicy Bypass -File run-dev.ps1

$ErrorActionPreference = 'Stop'
$envFile = Join-Path $PSScriptRoot '.env'

if (-not (Test-Path $envFile)) {
    Write-Host "[ERROR] 未找到 $envFile ，请先创建并填写 NAS 连接信息" -ForegroundColor Red
    exit 1
}

# 逐行解析 .env（KEY=VALUE），注入当前进程环境变量
Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { return }
    $idx = $line.IndexOf('=')
    if ($idx -lt 1) { return }
    $key = $line.Substring(0, $idx).Trim()
    $val = $line.Substring($idx + 1).Trim()
    [Environment]::SetEnvironmentVariable($key, $val, 'Process')
}
Write-Host "[OK] 已加载 .env 环境变量" -ForegroundColor Green

# JDK 17
$env:JAVA_HOME = 'C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot'
if (-not (Test-Path $env:JAVA_HOME)) {
    Write-Host "[ERROR] 未找到 JDK 17: $env:JAVA_HOME" -ForegroundColor Red
    exit 1
}
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Maven 查找：优先 PATH，其次常见安装位置（@() 强制数组化，避免单元素被解包成标量）
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvn) {
    $candidates = @(
        'C:\Program Files\Apache\Maven\bin',
        'C:\Program Files\apache-maven\bin',
        'C:\apache-maven\bin',
        'C:\tools\apache-maven\bin',
        'D:\apache-maven\bin',
        'D:\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin',
        'D:\apache-maven-3.10.0-bin\bin',
        'D:\Program Files\Apache\Maven\bin',
        'D:\tools\apache-maven\bin'
    ) | Where-Object { Test-Path (Join-Path $_ 'mvn.cmd') }
    if (@($candidates).Count -eq 0) {
        Write-Host "[ERROR] 未找到 Maven，请先安装 apache-maven-3.9.x 并解压到 C:\Program Files\Apache\Maven" -ForegroundColor Red
        exit 1
    }
    $env:Path = "$($env:JAVA_HOME)\bin;$(@($candidates)[0]);$env:Path"
}

Write-Host "[OK] 启动 Spring Boot (端口 8080, context /api/v1) ..." -ForegroundColor Green
Set-Location $PSScriptRoot
mvn spring-boot:run
