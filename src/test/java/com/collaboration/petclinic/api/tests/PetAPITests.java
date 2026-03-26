package com.collaboration.petclinic.api.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

// ADD PET FUNCTIONALITY TESTS


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetAPITests {

    private static int createdPetId; // To store the ID of the pet created in the first test

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:9966/petclinic";
        RestAssured.authentication = RestAssured.basic("admin", "admin");
    }


    @Test
    @Order(1)
    @DisplayName("TC001 - Add a new pet successfully - expected status 201")
    public void testAddPetSuccessfully() {
        String petJson = "{\"name\": \"Buddy\", \"birthDate\": \"2022-05-15\", \"type\": {\"id\": 2, \"name\": \"dog\"}, \"ownerId\": 1}";

        createdPetId = given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201)
                .body("name", equalTo("Buddy"))
                .body("birthDate", equalTo("2022-05-15"))
                .body("type.name", equalTo("dog"))
                .body("ownerId", equalTo(1))
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    @DisplayName("TC002 - Add a pet with today's birth date - expected status 201")
    public void testAddPetWithBirthDateToday() {
        String today = java.time.LocalDate.now().toString();
        String petJson = "{\"name\": \"Rex\", \"birthDate\": \"" + today + "\", \"type\": {\"id\": 1, \"name\": \"cat\"}, \"ownerId\": 1}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201)
                .body("birthDate", equalTo(today));
    }

    @Test
    @Order(3)
    @DisplayName("TC005 - Add a pet with special characters in the name - expected status 201")
    // Linked to BUG004 - needs clarification from team
    public void testAddPetWithSpecialCharacters() {
        String petJson = "{\"name\": \"#!@123\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 1, \"name\": \"cat\"}, \"ownerId\": 1}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(4)
    @DisplayName("TC008 - Add two pets with the same name to the same owner - expected status 201")
    // Linked to BUG005 - needs clarification from team
    public void testAddDuplicatePetNameForSameOwner() {
        String petJson = "{\"name\": \"Bella\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 1, \"name\": \"cat\"}, \"ownerId\": 1}";

        given().contentType(ContentType.JSON).body(petJson).post("/api/owners/1/pets");

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(5)
    @DisplayName("TC011 - Get an existing pet by valid ID - expected status 200")
    public void testGetPetByValidId() {
        // Pet with ID 1 (Leo) exists in the system by default
        given()
                .when()
                .get("/api/pets/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", notNullValue())
                .body("type", notNullValue())
                .body("ownerId", notNullValue());
    }

    @Test
    @Order(6)
    @DisplayName("TC013 - Update an existing pet's details - expected status 204")
    public void testUpdatePet() {
        // 204 means success with no content returned
        String updatedPetJson = "{\"name\": \"BuddyUpdated\", \"birthDate\": \"2022-05-15\", \"type\": {\"id\": 2, \"name\": \"dog\"}}";

        given()
                .contentType(ContentType.JSON)
                .body(updatedPetJson)
                .when()
                .put("/api/owners/1/pets/" + createdPetId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(7)
    @DisplayName("TC014 - Delete a pet - expected status 204 and pet should no longer exist")
    public void testDeletePet() {
        // Step 1: Create a pet specifically to delete so we don't affect other tests
        String petJson = "{\"name\": \"PetToDelete\", \"birthDate\": \"2021-06-10\", \"type\": {\"id\": 3, \"name\": \"lizard\"}}";

        int petToDeleteId = given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Step 2: Delete the pet
        given()
                .when()
                .delete("/api/pets/" + petToDeleteId)
                .then()
                .statusCode(204);

        // Step 3: Verify the pet no longer exists
        given()
                .when()
                .get("/api/pets/" + petToDeleteId)
                .then()
                .statusCode(404);
    }


    @Test
    @Order(8)
    @DisplayName("TC003 - Add a pet with a future birth date - expected status 400")
    public void testAddPetWithFutureDate_ShouldReturn400() {
        String petJson = "{\"name\": \"Futuristico\", \"birthDate\": \"2029-01-15\", \"type\": {\"id\": 1, \"name\": \"cat\"}, \"ownerId\": 2}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(9)
    @DisplayName("TC004 - Add a pet without a name - expected status 400")
    // BUG001: System currently returns 500 instead of expected 400
    public void testAddPetWithoutName_ShouldReturn400() {
        String petJson = "{\"name\": \"\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 2, \"name\": \"dog\"}, \"ownerId\": 1}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(10)
    @DisplayName("TC015 - Add a pet with spaces only as name - expected status 400")
    // BUG006: System returns 404 with DataIntegrityViolationException
    public void testAddPetWithSpacesOnlyName_ShouldReturn400() {
        String petJson = "{\"name\": \"   \", \"birthDate\": \"2022-05-10\", \"type\": {\"id\": 2, \"name\": \"dog\"}}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(11)
    @DisplayName("TC005_NEG - Add a pet without type - expected status 400")
    public void testAddPetWithoutType_ShouldReturn400() {
        // Type is a required field per acceptance criteria
        String petJson = "{\"name\": \"NoTypePet\", \"birthDate\": \"2022-05-10\"}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    @DisplayName("TC006 - Add a pet without birth date - expected status 400")
    public void testAddPetWithoutBirthDate_ShouldReturn400() {
        // Birth date is a required field per acceptance criteria
        String petJson = "{\"name\": \"NoBirthDatePet\", \"type\": {\"id\": 2, \"name\": \"dog\"}}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(13)
    @DisplayName("TC007 - Add a pet to a non-existent owner - expected status 404")
    public void testAddPetToNonExistentOwner_ShouldReturn404() {
        // Owner with ID 999999 does not exist in the system
        String petJson = "{\"name\": \"OrphanPet\", \"birthDate\": \"2022-05-10\", \"type\": {\"id\": 2, \"name\": \"dog\"}}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/999999/pets")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("TC012 - Get a pet by non-existent ID - expected status 404")
    public void testGetPetByNonExistentId_ShouldReturn404() {
        // Pet with ID 999999 does not exist in the system
        given()
                .when()
                .get("/api/pets/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(15)
    @DisplayName("TC007_INV - Add a pet with invalid pet type ID - expected status 400")
    public void testAddPetWithInvalidType_ShouldFail() {
        // Pet type with ID 999 does not exist in the system
        String petJson = "{\"name\": \"Spyro\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 999, \"name\": \"dragon\"}, \"ownerId\": 1}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }
}