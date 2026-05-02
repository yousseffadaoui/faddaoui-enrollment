# Start course-service (8083) and enrollment-service (8084)
# No Docker or Eureka required - uses H2 databases
# Usage: .\run-services.ps1

param(
    [switch]$StopOnly,
    [switch]$SkipFrontend
)

Set-Location $PSScriptRoot
$base = $PSScriptRoot

function Stop-Port {
    param([int]$port)
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        $procIds = $conn | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($procId in $procIds) {
            Write-Host "Stopping process $procId on port $port..."
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}

# Kill existing processes on 8083 and 8084
if (-not $StopOnly) {
    Write-Host "Clearing ports 8083 and 8084..."
    Stop-Port -port 8083
    Stop-Port -port 8084
    Start-Sleep -Seconds 2
}

if ($StopOnly) {
    Write-Host "Ports cleared. Run without -StopOnly to start services."
    exit 0
}

Write-Host ""
Write-Host "Starting course-service (port 8083)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\course-service'; .\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 15

Write-Host "Starting enrollment-service (port 8084)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\Enrollment-service'; .\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 15

if (-not $SkipFrontend) {
    Write-Host "Starting Angular frontend (port 4202)..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$base\esm-front-main'; ng serve --port 4202"
}

Write-Host ""
Write-Host "Services started:"
Write-Host "  course-service:     http://localhost:8083"
Write-Host "  enrollment-service: http://localhost:8084"
if (-not $SkipFrontend) {
    Write-Host "  Frontend:            http://localhost:4202"
}
Write-Host ""
Write-Host "To stop: .\run-services.ps1 -StopOnly"
Write-Host "Docker (persistent DB): .\run-docker-persist.ps1"
