# ====================================================================
# 创融通 M3 冒烟测试：AIConfig / AI 业务端点 / 图谱 / 总览
# 用法: powershell -ExecutionPolicy Bypass -File scripts\smoke-m3.ps1
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
        $kw = @{ Uri = "$base$url"; Method = $method; Headers = $hdr; ContentType = 'application/json'; TimeoutSec = 90 }
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
$adm = Login 'platform_admin'

Write-Host '===== 1. Admin 总览 =====' -ForegroundColor Cyan
Call '总览-admin' 'GET' '/admin/stats/overview' $null $adm 't-platform'
Assert '总览-6个KPI' ($global:lastData.kpis.Count -eq 6)
Assert '总览-趋势3条线' ($global:lastData.trend.series.Count -eq 3)
Call '总览-investor越权' 'GET' '/admin/stats/overview' $null $inv 't-001' 403001

Write-Host '===== 2. AIConfig Prompt 模板 =====' -ForegroundColor Cyan
Call 'Prompt列表-admin' 'GET' '/admin/ai/prompts' $null $adm 't-platform'
Assert 'Prompt-种子5条' ($global:lastData.Count -eq 5)
Call 'Prompt列表-investor越权' 'GET' '/admin/ai/prompts' $null $inv 't-001' 403001
Call 'Prompt新增' 'POST' '/admin/ai/prompts' '{"name":"冒烟测试模板","scene":"chat","model":"gpt-4o-mini","promptTemplate":"请简洁回答"}' $adm 't-platform'
$newPid = $global:lastData.id
Call 'Prompt更新' 'PUT' "/admin/ai/prompts/$newPid" '{"name":"冒烟测试模板v2","enabled":false}' $adm 't-platform'
Call 'Prompt删除' 'DELETE' "/admin/ai/prompts/$newPid" $null $adm 't-platform'

Write-Host '===== 3. AIConfig 模型路由 =====' -ForegroundColor Cyan
Call '模型路由列表-admin' 'GET' '/admin/ai/models' $null $adm 't-platform'
Assert '模型路由-至少5条' ($global:lastData.Count -ge 5)
Call '模型路由保存(upsert)' 'POST' '/admin/ai/models' '{"routeKey":"smoke_route","scene":"冒烟","primaryModel":"gpt-4o-mini","fallbackModel":"","temperature":0.5,"maxTokens":1024}' $adm 't-platform'
Call '模型路由更新' 'PUT' '/admin/ai/models/smoke_route' '{"temperature":0.6}' $adm 't-platform'
Call '模型路由更新-不存在' 'PUT' '/admin/ai/models/no_such_route' '{"temperature":0.6}' $adm 't-platform' 404000

Write-Host '===== 4. AI 业务端点 =====' -ForegroundColor Cyan
Call 'AI项目筛选' 'POST' '/ai/project-filter' '{"industry":"ai","stage":"seed","amount":500}' $inv 't-001'
Assert '筛选-返回结果非空' ($global:lastData.results.Count -gt 0)
Assert '筛选-维度完整' (@($global:lastData.results[0].dimensions.PSObject.Properties).Count -eq 3)
Call 'AI评审简报' 'POST' '/ai/review-brief' '{"projectId":"p-001"}' $inv 't-001'
Assert '简报-4个章节' ($global:lastData.sections.Count -eq 4)
Assert '简报-总体评分' ($global:lastData.overallScore -gt 0)
Call 'AI对话' 'POST' '/ai/chat' '{"prompt":"如何准备路演？"}' $inv 't-001'
Assert '对话-有回复与会话ID' ($global:lastData.reply.Length -gt 0 -and $global:lastData.sessionId.Length -gt 0)
Call 'AI接口-无token' 'POST' '/ai/chat' '{"prompt":"hi"}' $null 't-001' 401000

Write-Host '===== 5. 关系图谱 =====' -ForegroundColor Cyan
Call '图谱-全量' 'GET' '/graph' $null $inv 't-001'
Assert '图谱-节点非空' ($global:lastData.nodes.Count -gt 0)
Assert '图谱-边非空' ($global:lastData.edges.Count -gt 0)
Call '图谱-中心邻域' 'GET' '/graph?centerId=inst-001&depth=2' $null $inv 't-001'
Assert '图谱-邻域含中心节点' (@($global:lastData.nodes | Where-Object { $_.id -eq 'inst-001' }).Count -eq 1)
Call '图谱-无token' 'GET' '/graph' $null $null 't-001' 401000

Write-Host ''
Write-Host ("结果: PASS={0} FAIL={1}" -f $script:pass, $script:fail) -ForegroundColor ($(if ($script:fail -eq 0) { 'Green' } else { 'Red' }))
