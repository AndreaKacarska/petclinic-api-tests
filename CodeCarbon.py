from codecarbon import EmissionsTracker
import subprocess
import os

MVN = r"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd"

TEST_CLASSES = [
    "OwnerAPITest",
    "PetAPITests",
    "VeterinarianApiTest",
    "VisitTest"
]

for test_class in TEST_CLASSES:
    print(f"\n=== Running {test_class} ===")

    output_dir = os.path.join("codecarbon-result", test_class)
    os.makedirs(output_dir, exist_ok=True)

    tracker = EmissionsTracker(
        project_name=f"PetClinic-{test_class}",
        output_dir=output_dir,
        output_file="emissions.csv"
    )

    tracker.start()
    subprocess.run(f'"{MVN}" test -Dtest={test_class} -f pom.xml', shell=True)
    emissions = tracker.stop()

    print(f"{test_class} → {emissions:.6f} kg CO2eq | saved to {output_dir}")

print("\nAll tests complete.")