# Running Energy Measurements with JoularJX

## Manual — Single Test Class

Run from the project root in IntelliJ's terminal:

For `OwnerAPITest`:
```bash
mvn test -Dtest=OwnerAPITest -DargLine="-javaagent:joularjx-3.1.0.jar"
```

For `PetAPITests`:
```bash
mvn test -Dtest=PetAPITests -DargLine="-javaagent:joularjx-3.1.0.jar"
```

For `VeterinarianApiTest`:
```bash
mvn test -Dtest=VeterinarianApiTest -DargLine="-javaagent:joularjx-3.1.0.jar"
```

For `VisitTest`:
```bash
mvn test -Dtest=VisitTest -DargLine="-javaagent:joularjx-3.1.0.jar"
```

---

## Automated — All Test Classes via Script

Edit the test class list at the top of `run-energy-tests.ps1`:

```powershell
$testClasses = @(
    "OwnerAPITest",
    "PetAPITests"
    
    .
    .
    .
    
foreach ($testClass in $testClasses) {
    Write-Host "`n=== Running $testClass ===" -ForegroundColor Cyan

    & $mvn test "-Dtest=$testClass" "-DargLine=-javaagent:joularjx-3.1.0.jar" -f pom.xml
    
    .
    .
```

Then run from the project root in PowerShell:

```powershell
.\run-energy-tests.ps1
```

Each test class runs one by one. Results are saved to `joularjx-result\<TestClassName>\<timestamp>\`.