# Start persistent stack: PostgreSQL + course-service + enrollment-service
# Courses and enrollments persist across restarts
# Usage: .\run-docker-persist.ps1
# Force full rebuild (use if certificate design doesn't apply): .\run-docker-persist.ps1 -Rebuild

param([switch]$Rebuild)
Set-Location $PSScriptRoot
if ($Rebuild) {
  docker-compose -f docker-compose.persist.yml build --no-cache
}
docker-compose -f docker-compose.persist.yml up -d --build
Write-Host ""
Write-Host "Services started. Data persists in Docker volumes."
Write-Host "  course-service:     http://localhost:8083"
Write-Host "  enrollment-service: http://localhost:8084"
Write-Host ""
Write-Host "Stop: docker-compose -f docker-compose.persist.yml down"
Write-Host "Logs: docker-compose -f docker-compose.persist.yml logs -f"
