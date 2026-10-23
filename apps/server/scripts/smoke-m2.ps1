# ====================================================================
# 创融通 M2 冒烟测试：全端点契约 + 三角色越权矩阵
# 用法: powershell -ExecutionPolicy Bypass -File scripts\smoke-m2.ps1
# ====================================================================
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api/v1'
$script:pass = 0; $script:fail = 0

function Login($role) {
    $body = '{"role":"' + $role + '"}'
    $r = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -Body $body -ContentType 'application/json'
    return $r.data.token
}

# method,url,body,token,tenantId,expectCode(默认 200)
function Call($name, $method, $url, $body, $token, $tenantId, $expectCode = 200) {
    try {
        $hdr = @{ Authorization = "Bearer $token" }
        if ($tenantId) { $hdr['X-Tenant-ID'] = $tenantId }
        $kw = @{ Uri = "$base$url"; Method = $method; Headers = $hdr; ContentType = 'application/json'; TimeoutSec = 10 }
        if ($body) { $kw.Body = [System.Text.Encoding]::UTF8.GetBytes($body) }
        $r = Invoke-RestMethod @kw
        $got = $r.code
        $detail = ($r.data | ConvertTo-Json -Compress -Depth 3)
        if ($detail.Length -gt 110) { $detail = $detail.Substring(0, 110) }
        if ($got -eq $expectCode) { $script:pass++; Write-Host ("PASS  {0}  ->  {1}" -f $name, $detail) -ForegroundColor Green }
        else { $script:fail++; Write-Host ("FAIL  {0}  code={1} expect={2}  {3}" -f $name, $got, $expectCode, $detail) -ForegroundColor Red }
    } catch {
        $msg = $_.Exception.Message
        if ($msg.Length -gt 120) { $msg = $msg.Substring(0, 120) }
        $script:fail++; Write-Host ("ERR   {0}  {1}" -f $name, $msg) -ForegroundColor Red
    }
}

Write-Host '===== 1. Auth 域（公开端点） =====' -ForegroundColor Cyan
Call '一键体验-entrepreneur' 'POST' '/auth/login' '{"role":"entrepreneur"}' $null $null
Call '一键体验-investor'     'POST' '/auth/login' '{"role":"investor"}' $null $null
Call '一键体验-admin'        'POST' '/auth/login' '{"role":"platform_admin"}' $null $null
Call '发送验证码'            'POST' '/auth/send-code' '{"phone":"13800138003"}' $null $null
Call '注册'                  'POST' '/auth/register' '{"role":"entrepreneur","phone":"13800001111","code":"123456"}' $null $null
Call '重置密码'              'POST' '/auth/reset-password' '{"phone":"13800001111","code":"123456","newPassword":"abc12345"}' $null $null
Call '微信登录'              'POST' '/auth/wechat/login' '{}' $null $null

$inv = Login 'investor'       # 李投资 t-001
$adm = Login 'platform_admin' # u-001 t-platform
$ent = Login 'entrepreneur'   # 张创业 t-001

Write-Host '===== 2. Auth 域（需 token） =====' -ForegroundColor Cyan
Call 'me-investor' 'GET' '/auth/me' $null $inv 't-001'
Call 'refresh'     'POST' '/auth/refresh' $null $inv 't-001'

Write-Host '===== 3. Tenant 域 =====' -ForegroundColor Cyan
Call '租户列表-admin'    'GET'  '/tenants?pageNum=1&pageSize=20' $null $adm 't-platform'
Call '租户列表-investor越权' 'GET' '/tenants' $null $inv 't-001' 403001
Call '租户详情-admin'    'GET'  '/tenants/t-001' $null $adm 't-platform'
$created = Invoke-RestMethod -Uri "$base/tenants" -Method Post -Headers @{Authorization="Bearer $adm"; 'X-Tenant-ID'='t-platform'} -ContentType 'application/json' -Body '{"name":"测试租户TC","type":"institution","status":"trial","contactName":"测试","contactPhone":"13000000000"}'
$newTid = $created.data.id
Call '租户开通-admin'    'GET'  "/tenants/$newTid" $null $adm 't-platform'
Call '租户状态-admin'    'PUT'  "/tenants/$newTid/status" '{"status":"active"}' $adm 't-platform'

Write-Host '===== 4. Institution 域 =====' -ForegroundColor Cyan
Call '机构列表'   'GET' '/institutions?pageNum=1&pageSize=12' $null $inv 't-001'
Call '机构详情'   'GET' '/institutions/inst-001' $null $inv 't-001'

Write-Host '===== 5. Blacklist 域 =====' -ForegroundColor Cyan
Call '黑名单列表-admin' 'GET' '/blacklist?pageNum=1&pageSize=20' $null $adm 't-platform'
Call '黑名单列表-investor越权' 'GET' '/blacklist' $null $inv 't-001' 403001
Call '黑名单新增-admin' 'POST' '/blacklist' '{"type":"phone","targetValue":"139****0000","reason":"冒烟测试临时条目"}' $adm 't-platform'
Call '黑名单删除-admin' 'DELETE' '/blacklist/bl-005' $null $adm 't-platform'

Write-Host '===== 6. Project 域 =====' -ForegroundColor Cyan
Call '项目列表-investor(公开市场+本租户)' 'GET' '/projects?pageNum=1&pageSize=10' $null $inv 't-001'
Call '项目列表-admin(全量)'               'GET' '/projects' $null $adm 't-platform'
Call '项目详情'      'GET' '/projects/p-001' $null $inv 't-001'
Call '项目详情-published跨租户可见(p-003)' 'GET' '/projects/p-003' $null $ent 't-001'
Call '项目详情-跨租户越权(p-rv-002未发布属t-002)' 'GET' '/projects/p-rv-002' $null $ent 't-001' 403000
Call 'AI诊断'        'POST' '/projects/diagnosis' '{}' $ent 't-001'
$np = Invoke-RestMethod -Uri "$base/projects" -Method Post -Headers @{Authorization="Bearer $ent"; 'X-Tenant-ID'='t-001'} -ContentType 'application/json' -Body ([System.Text.Encoding]::UTF8.GetBytes('{"title":"冒烟测试项目","industry":"ai","region":"东莞","stage":"seed","amountSought":500,"tags":["测试"]}'))
$newPid = $np.data.id
Call '项目创建-返回'  'GET' "/projects/$newPid" $null $ent 't-001'
Call '项目更新'      'PUT'  "/projects/$newPid" '{"title":"冒烟测试项目v2","industry":"ai","region":"东莞","stage":"seed","amountSought":600}' $ent 't-001'
Call '项目提交审核'  'POST' "/projects/$newPid/submit" $null $ent 't-001'
Call '项目提交-重复提交应拒' 'POST' "/projects/$newPid/submit" $null $ent 't-001' 400301
Call '项目更新-跨租户越权' 'PUT' "/projects/p-rv-002" '{"title":"越权"}' $ent 't-001' 403000

Write-Host '===== 7. Admin 项目审核 =====' -ForegroundColor Cyan
Call '审核队列-admin(pending)' 'GET' '/admin/projects/reviews?status=pending' $null $adm 't-platform'
Call '审核队列-investor越权'   'GET' '/admin/projects/reviews' $null $inv 't-001' 403001
Call '审核通过-rv-001'  'PUT' '/admin/projects/reviews/p-rv-001' '{"decision":"pass"}' $adm 't-platform'
Call '审核驳回-rv-003'  'PUT' '/admin/projects/reviews/p-rv-003' '{"decision":"reject","reason":"团队信息不完整"}' $adm 't-platform'

Write-Host '===== 8. Roadshow 域 =====' -ForegroundColor Cyan
Call '路演列表'      'GET' '/roadshows?pageNum=1&pageSize=12' $null $inv 't-001'
Call '路演详情'      'GET' '/roadshows/r-001' $null $inv 't-001'
Call '路演报名'      'POST' '/roadshows/r-001/apply' '{"projectId":"p-001"}' $ent 't-001'
$nr = Invoke-RestMethod -Uri "$base/roadshows" -Method Post -Headers @{Authorization="Bearer $adm"; 'X-Tenant-ID'='t-platform'} -ContentType 'application/json' -Body ([System.Text.Encoding]::UTF8.GetBytes('{"title":"冒烟测试路演","type":"online","isClosedDoor":false,"startTime":"2026-12-20 10:00","endTime":"2026-12-20 12:00","maxParticipants":100}'))
$newRid = $nr.data.id
Call '路演创建-返回' 'GET' "/roadshows/$newRid" $null $adm 't-platform'
Call '路演更新'      'PUT'  "/roadshows/$newRid" '{"status":"published"}' $adm 't-platform'

Write-Host '===== 9. Knowledge 域 =====' -ForegroundColor Cyan
Call '知识列表'      'GET' '/knowledge?pageNum=1&pageSize=20' $null $inv 't-001'
Call '知识详情'      'GET' '/knowledge/kn-001' $null $inv 't-001'
$nk = Invoke-RestMethod -Uri "$base/knowledge" -Method Post -Headers @{Authorization="Bearer $adm"; 'X-Tenant-ID'='t-platform'} -ContentType 'application/json' -Body ([System.Text.Encoding]::UTF8.GetBytes('{"title":"冒烟测试知识条目","category":"测试","content":"内容","tags":["smoke"]}'))
$newKid = $nk.data.id
Call '知识更新'      'PUT' "/knowledge/$newKid" '{"title":"冒烟测试知识条目v2"}' $adm 't-platform'
Call '知识下架'      'PATCH' "/knowledge/$newKid/publish" '{"published":false}' $adm 't-platform'
Call '知识删除'      'DELETE' "/knowledge/$newKid" $null $adm 't-platform'

Write-Host '===== 10. Order 域 =====' -ForegroundColor Cyan
Call '订单列表-admin(全量5)'     'GET' '/orders?pageNum=1&pageSize=20' $null $adm 't-platform'
Call '订单列表-investor(t-001过滤)' 'GET' '/orders' $null $inv 't-001'
Call '订单详情'      'GET' '/orders/ORD20261019001' $null $inv 't-001'

Write-Host '===== 11. 租户头不一致拦截（TenantInterceptor） =====' -ForegroundColor Cyan
Call 'investor-token+错误租户头' 'GET' '/projects' $null $inv 't-002' 403001
Call '无token访问业务域'         'GET' '/projects' $null $null 't-001' 401000

Write-Host ''
Write-Host ("结果: PASS={0} FAIL={1}" -f $script:pass, $script:fail) -ForegroundColor ($(if ($script:fail -eq 0) { 'Green' } else { 'Red' }))
