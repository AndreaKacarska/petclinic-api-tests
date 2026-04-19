from codecarbon import EmissionsTracker
import subprocess

tracker = EmissionsTracker()
tracker.start()

subprocess.run(["cmd", "/c", "mvnw", "test"])

tracker.stop()

