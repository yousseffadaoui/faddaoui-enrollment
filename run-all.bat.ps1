# ESM - Run all services
# Run each command in a NEW terminal (or use Start-Process)

param(
    [switch]$StopOnly,
    [switch]$SkipKill
)

$ports = @(8761, 8083, 8084, 4202)

function Stop-Port {
    param([int]$port)
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        $procIds = $conn | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($procId in $procIds) {
            Write-Host "Stopping process $procId (port $port)..."
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}

if (-not $SkipKill) {
    Write-Host "Stopping processes on ports: $($ports -join ', ')"
    foreach ($p in $ports) { Stop-Port -port $p }
    Start-Sleep -Seconds 3
}
if ($StopOnly) { exit 0 }

$base = $PSScriptRoot
Write-Host "Starting ESM services from $base"

# Start Eureka
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\Eureka-main'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15

# Start Course service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\course-service'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 25

# Start Enrollment service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\Enrollment-service'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 25

# Start Angular frontend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\esm-front-main'; ng serve --port 4202" -WindowStyle Normal

Write-Host "`nAll services started. Open http://localhost:4202/#/dashboard_admin"
Write-Host "Eureka: http://localhost:8761 | Course API: 8083 | Enrollment API: 8084"
