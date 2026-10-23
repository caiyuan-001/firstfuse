# ====================================================================
# 创融通 M4 冒烟测试：订单导出 / 文件上传下载 / 操作日志 / 限流
# 用法: powershell -ExecutionPolicy Bypass -File scripts\smoke-m4.ps1
# 注意：限流用例会产生约 100 次请求，脚本尾部执行；建议独立运行，
#       与 smoke-m2/m3 间隔 1 分钟以上（共享登录限流窗口）
# ====================================================================
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api/v1'
$script:pass = 0; $script:fail = 0

function Login($role) {
    $body = '{"role":"' + $role + '"}'
    $r = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -Body $body -ContentType 'application/json'
    return $r.data.token
}

function Call($name, $method, $url, $body, $token, $tenantId, $expectCode = 200) {
    $global:lastData = $null
    try {
        $hdr = @{ Authorization = "Bearer $token" }
        if ($tenantId) { $hdr['X-Tenant-ID'] = $tenantId }
        $kw = @{ Uri = "$base$url"; Method = $method; Headers = $hdr; ContentType = 'application/json'; TimeoutSec = 15 }
        if ($body) { $kw.Body = [System.Text.Encoding]::UTF8.GetBytes($body) }
        $r = Invoke-RestMethod @kw
        $got = $r.code
        $global:lastData = $r.data
        $detail = ($r.data | ConvertTo-Json -Compress -Depth 2)
        if ($detail.Length -gt 110) { $detail = $detail.Substring(0, 110) }
        if ($got -eq $expectCode) { $script:pass++; Write-Host ("PASS  {0}  ->  {1}" -f $name, $detail) -ForegroundColor Green }
        else { $script:fail++; Write-Host ("FAIL  {0}  code={1} expect={2}  {3}" -f $name, $got, $expectCode, $detail) -ForegroundColor Red }
    } catch {
        $msg = $_.Exception.Message
        if ($msg.Length -gt 120) { $msg = $msg.Substring(0, 120) }
        $script:fail++; Write-Host ("ERR   {0}  {1}" -f $name, $msg) -ForegroundColor Red
    }
}

function Assert($name, $cond) {
    if ($cond) { $script:pass++; Write-Host ("PASS  {0}" -f $name) -ForegroundColor Green }
    else { $script:fail++; Write-Host ("FAIL  {0}" -f $name) -ForegroundColor Red }
}

$inv = Login 'investor'
$ent = Login 'entrepreneur'
$adm = Login 'platform_admin'

Write-Host '===== 1. 订单导出 CSV =====' -ForegroundColor Cyan
$csvOut = Join-Path $env:TEMP 'm4-orders.csv'
curl.exe -s -o $csvOut -w '%{http_code}' "$base/admin/orders/export" -H "Authorization: Bearer $adm" -H "X-Tenant-ID: t-platform" | Out-Null
$csvBytes = [System.IO.File]::ReadAllBytes($csvOut)
$csvText = [System.Text.Encoding]::UTF8.GetString($csvBytes)
Assert '导出-非空且含表头' ($csvText.Contains([char]0x8BA2 + [char]0x5355 + [char]0x53F7) -and $csvText.Length -gt 50)
Assert '导出-含数据行' (@($csvText -split "`r?`n").Count -ge 2)
$csvInv = curl.exe -s "$base/admin/orders/export" -H "Authorization: Bearer $inv" -H "X-Tenant-ID: t-001" | ConvertFrom-Json
Assert '导出-investor越权403001' ($csvInv.code -eq 403001)

Write-Host '===== 2. 文件上传/下载 =====' -ForegroundColor Cyan
$testFile = Join-Path $env:TEMP 'm4-upload.txt'
Set-Content -Path $testFile -Value 'firstfuse-m4-smoke' -Encoding ASCII
$up = curl.exe -s "$base/files" -H "Authorization: Bearer $inv" -H "X-Tenant-ID: t-001" --form "file=@$testFile" | ConvertFrom-Json
Assert '上传-成功' ($up.code -eq 200)
$fid = $up.data.id
Assert '上传-返回元数据' ($fid.Length -gt 0 -and $up.data.fileName -eq 'm4-upload.txt' -and $up.data.url -eq "/files/$fid")
$dl = curl.exe -s "$base/files/$fid" -H "Authorization: Bearer $inv" -H "X-Tenant-ID: t-001"
Assert '下载-内容一致' ($dl -eq 'firstfuse-m4-smoke')
Call '文件元数据' 'GET' "/files/$fid/info" $null $inv 't-001'
Assert '元数据-size正确' ($global:lastData.size -eq 20)
Call '文件-跨租户越权' 'GET' "/files/$fid/info" $null $ent 't-002' 403001
Call '文件-不存在' 'GET' '/files/f-notexist/info' $null $inv 't-001' 404000
Call '文件-无token' 'GET' "/files/$fid/info" $null $null 't-001' 401000

Write-Host '===== 3. 操作日志 =====' -ForegroundColor Cyan
Call '日志列表-admin' 'GET' '/admin/logs?pageSize=10' $null $adm 't-platform'
Assert '日志-有记录' ($global:lastData.total -gt 0)
Assert '日志-记录上传动作' (@($global:lastData.list | Where-Object { $_.path -like '*/files*' }).Count -ge 1)
Assert '日志-字段完整' ($global:lastData.list[0].method.Length -gt 0 -and $null -ne $global:lastData.list[0].statusCode)
Call '日志-按path筛选' 'GET' '/admin/logs?path=/files' $null $adm 't-platform'
Assert '日志-筛选生效' (@($global:lastData.list | Where-Object { $_.path -notlike '*/files*' }).Count -eq 0)
Call '日志-investor越权' 'GET' '/admin/logs' $null $inv 't-001' 403001

Write-Host '===== 4. 限流（登录 60/min、AI 30/min）=====' -ForegroundColor Cyan
Set-Content -Path "$env:TEMP\m4-login.json" -Value '{"role":"platform_admin"}' -Encoding ASCII
$loginCodes = 1..65 | ForEach-Object {
    (curl.exe -s "$base/auth/login" -H 'Content-Type: application/json' --data-binary "@$env:TEMP\m4-login.json" | ConvertFrom-Json).code
}
$okCount = @($loginCodes | Where-Object { $_ -eq 200 }).Count
$rlCount = @($loginCodes | Where-Object { $_ -eq 401103 }).Count
Assert "登录限流-前60放行(实际$okCount)" ($okCount -le 60)
Assert "登录限流-触发401103(实际$rlCount)" ($rlCount -ge 1)

# 等登录窗口结束，避免影响后续
Start-Sleep -Seconds 65
$inv2 = Login 'investor'
Set-Content -Path "$env:TEMP\m4-ai.json" -Value '{"industry":"ai"}' -Encoding ASCII
$aiCodes = 1..33 | ForEach-Object {
    (curl.exe -s "$base/ai/project-filter" -H 'Content-Type: application/json' -H "Authorization: Bearer $inv2" -H 'X-Tenant-ID: t-001' --data-binary "@$env:TEMP\m4-ai.json" | ConvertFrom-Json).code
}
$aiOk = @($aiCodes | Where-Object { $_ -eq 200 }).Count
$aiRl = @($aiCodes | Where-Object { $_ -eq 401103 }).Count
Assert "AI限流-前30放行(实际$aiOk)" ($aiOk -le 30)
Assert "AI限流-触发401103(实际$aiRl)" ($aiRl -ge 1)

Write-Host ''
$color = 'Green'; if ($script:fail -gt 0) { $color = 'Red' }
Write-Host ("===== M4 结果: {0} PASS / {1} FAIL =====" -f $script:pass, $script:fail) -ForegroundColor $color
if ($script:fail -eq 0) { exit 0 } else { exit 1 }
