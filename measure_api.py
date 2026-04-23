from codecarbon import EmissionsTracker
import subprocess

tracker = EmissionsTracker(project_name="PetClinic-API-Tests", output_file="api_with_AI_emissions.csv")

tracker.start()

# Run the API tests
# subprocess.run(
#     '"C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.3\\plugins\\maven\\lib\\maven3\\bin\\mvn" test -Dtest=OwnerAPITest',
#     shell=True
# )

# Run the AI-enhanced API test
subprocess.run(
    '"C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.3\\plugins\\maven\\lib\\maven3\\bin\\mvn" test -Dtest=OwnerAPITestWithAI',
    shell=True
)

tracker.stop()

print("API Test measurement complete. Results saved to api_emissions.csv")