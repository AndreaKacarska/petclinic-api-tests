# Running Energy Measurements with JoularJX

## Manual — Single Test Class

Run from the project root in IntelliJ's terminal:

For `OwnerAPITest`:
```bash
mvn test -Dtest=OwnerAPITest -DargLine="-javaagent:joularjx-3.1.0.jar"
```

For `OwnerAPITestWithAI`:
```bash
mvn test -Dtest=OwnerAPITestWithAI -DargLine="-javaagent:joularjx-3.1.0.jar"
```

---

## Automated — All Test Classes via Script

Edit the test class list at the top of `run-energy-tests.ps1`:

```powershell
$testClasses = @(
    "OwnerAPITest",
    "OwnerAPITestWithAI"
)

```

Then run from the project root in PowerShell:

```powershell
.\run-energy-tests.ps1
```

Each test class runs one by one. Results are saved to `joularjx-result\<TestClassName>\<timestamp>\`.