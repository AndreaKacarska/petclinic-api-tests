package com.collaboration.petclinic.api.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetAPITests {

    private static int createdPetId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:9966/petclinic";
        RestAssured.authentication = RestAssured.basic("admin", "admin");
        RestAssured.requestSpecification = new io.restassured.builder.RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    private RequestSpecification withBody(String json) {
        return given().body(json);
    }

    private String petJson(String name, String birthDate, int typeId, String typeName, Integer ownerId) {
        String ownerPart = ownerId != null ? ", \"ownerId\": " + ownerId : "";
        return String.format(
                "{\"name\": \"%s\", \"birthDate\": \"%s\", \"type\": {\"id\": %d, \"name\": \"%s\"}%s}",
                name, birthDate, typeId, typeName, ownerPart
        );
    }

    @Test
    @Order(1)
    @DisplayName("TC001 - Add a new pet successfully - expected status 201")
    public void testAddPetSuccessfully() {
        createdPetId = withBody(petJson("Buddy", "2022-05-15", 2, "dog", 1))
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

        withBody(petJson("Rex", today, 1, "cat", 1))
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
        withBody(petJson("#!@123", "2024-01-15", 1, "cat", 1))
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(4)
    @DisplayName("TC008 - Add two pets with the same name to the same owner - expected status 201")
    // Linked to BUG005 - needs clarification from team
    public void testAddDuplicatePetNameForSameOwner() {
        String json = petJson("Bella", "2024-01-15", 1, "cat", 1);

        withBody(json).post("/api/owners/1/pets");

        withBody(json)
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(5)
    @DisplayName("TC011 - Get an existing pet by valid ID - expected status 200")
    public void testGetPetByValidId() {
        given()
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
        withBody(petJson("BuddyUpdated", "2022-05-15", 2, "dog", null))
                .put("/api/owners/1/pets/" + createdPetId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(7)
    @DisplayName("TC014 - Delete a pet - expected status 204 and pet should no longer exist")
    public void testDeletePet() {
        int petToDeleteId = withBody(petJson("PetToDelete", "2021-06-10", 3, "lizard", null))
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given().delete("/api/pets/" + petToDeleteId).then().statusCode(204);
        given().get("/api/pets/" + petToDeleteId).then().statusCode(404);
    }

    @Test
    @Order(8)
    @DisplayName("TC003 - Add a pet with a future birth date - expected status 400")
    public void testAddPetWithFutureDate_ShouldReturn400() {
        withBody(petJson("Futuristico", "2029-01-15", 1, "cat", 2))
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(9)
    @DisplayName("TC004 - Add a pet without a name - expected status 500 (BUG001)")
    // BUG001: System currently returns 500 instead of expected 400
    public void testAddPetWithoutName_ShouldReturn400() {
        withBody(petJson("", "2024-01-15", 2, "dog", 1))
                .post("/api/owners/1/pets")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(10)
    @DisplayName("TC015 - Add a pet with spaces only as name - expected status 201 (BUG006)")
    // BUG006: System returns 201 instead of expected 400
    public void testAddPetWithSpacesOnlyName_ShouldReturn400() {
        withBody(petJson("   ", "2022-05-10", 2, "dog", null))
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(11)
    @DisplayName("TC005_NEG - Add a pet without type - expected status 400")
    public void testAddPetWithoutType_ShouldReturn400() {
        withBody("{\"name\": \"NoTypePet\", \"birthDate\": \"2022-05-10\"}")
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    @DisplayName("TC006 - Add a pet without birth date - expected status 400")
    public void testAddPetWithoutBirthDate_ShouldReturn400() {
        withBody("{\"name\": \"NoBirthDatePet\", \"type\": {\"id\": 2, \"name\": \"dog\"}}")
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(13)
    @DisplayName("TC007 - Add a pet to a non-existent owner - expected status 404")
    public void testAddPetToNonExistentOwner_ShouldReturn404() {
        withBody(petJson("OrphanPet", "2022-05-10", 2, "dog", null))
                .post("/api/owners/999999/pets")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("TC012 - Get a pet by non-existent ID - expected status 404")
    public void testGetPetByNonExistentId_ShouldReturn404() {
        given()
                .get("/api/pets/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(15)
    @DisplayName("TC007_INV - Add a pet with invalid pet type ID - expected status 404")
    public void testAddPetWithInvalidType_ShouldFail() {
        withBody("{\"name\": \"Spyro\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 999, \"name\": \"dragon\"}, \"ownerId\": 1}")
                .post("/api/owners/1/pets")
                .then()
                .statusCode(404);
    }
}