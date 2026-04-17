from codecarbon import EmissionsTracker
import subprocess

tracker = EmissionsTracker()
tracker.start()

subprocess.run("mvn test", shell=True)

tracker.stop()