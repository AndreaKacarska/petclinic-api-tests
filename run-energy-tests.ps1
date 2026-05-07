$mvn = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd"

$testClasses = @(
    "OwnerAPITest",
    "OwnerAPITestWithAI"
)

$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"

foreach ($testClass in $testClasses) {
    Write-Host "`n=== Running $testClass ===" -ForegroundColor Cyan

    & $mvn test "-Dtest=$testClass" "-DargLine=-javaagent:joularjx-3.1.0.jar" -f pom.xml

    $latest = Get-ChildItem -Path "joularjx-result" -Directory |
              Where-Object { $_.Name -notmatch "^[A-Za-z].*Test" } |
              Sort-Object LastWriteTime -Descending |
              Select-Object -First 1

    if ($latest) {
        $totalFolder = Join-Path $latest.FullName "app\total\methods"
        $destination = "joularjx-result\$testClass\$timestamp"
        New-Item -ItemType Directory -Path $destination -Force | Out-Null

        if (Test-Path $totalFolder) {
            Get-ChildItem -Path $totalFolder | Move-Item -Destination $destination
        }

        Remove-Item $latest.FullName -Recurse -Force
        Write-Host "Results saved to: $destination" -ForegroundColor Green
    }
}