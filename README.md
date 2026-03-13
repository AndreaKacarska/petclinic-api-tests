# REST API Test Automation project for Spring PetClinic application using Java and Rest Assured

API Test Automation project for the PetClinic application, built using Java, Rest Assured and JUnit; designed for 
the purpose of the academic collaboration between GD Netcetera and the FCSE in Skopje.

______________________

## Project purpose
- Supports learning and practice for students of the Faculty of Computer Science and Engineering in Skopje, as part of the academic collaboration.
- Provides a hands-on automation framework to test the REST APIs of the PetClinic application, a sample Spring Boot application for managing a veterinary clinic.
- Ensures basic validation of REST API endpoints for managing pets, owners, veterinarians, visits and specialties, including positive (happy path) and negative scenarios.

______________________

## Setup

### Tools & Requirements
Make sure that you have the following installed on your machine:
- JDK 21 - Required to build and run the tests
- Maven - Required for dependency management and test execution
- IntelliJ IDEA - Recommended for development and running tests
- Git - Required to clone, update and contribute to the project code

### Clone the PetClinic REST API TEST project
You are likely reading this from GitHub. To get started, clone this repository to your local machine:
```bash
git clone https://github.com/AndreaKacarska/petclinic-api-tests.git
```

### Clone the PetClinic REST API project 
Before running the test project, you have to start the backend service of the PetClinic application locally.

This backend version of the Spring PetClinic application only provides the REST API and the PetClinic REST API TEST project depends on the running backend. 
The UI is an Angular front-end application which consumes the REST API and the UI testing will be covered with another project.

#### Steps to clone and run the spring-petclinic-rest project locally:
```bash
git clone https://github.com/spring-petclinic/spring-petclinic-rest.git
```
*  Follow the README steps of the spring-petclinic-rest project to start the REST API service locally. 

### Running tests locally
Once everything is set up and the backend is running locally, you can run the API tests.
- Open the project in IntelliJ IDEA and add a Maven configuration [clean, test]
- Run the configuration to execute all the tests

