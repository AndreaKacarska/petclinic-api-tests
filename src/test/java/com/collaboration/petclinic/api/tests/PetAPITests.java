package com.collaboration.petclinic.api.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PetAPITests {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:9966/petclinic";
    }

    @Test
    public void testAddPetSuccessfully() {
        String petJson = "{\"name\": \"Bella\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 1, \"name\": \"dog\"}, \"ownerId\": 1}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201)
                .body("name", equalTo("Bella"));
    }

    @Test
    public void testAddPetWithFutureDate_ShouldFail() {
        // future date
        String petJson = "{\"name\": \"FutureDog\", \"birthDate\": \"2029-01-15\", \"type\": {\"id\": 1, \"name\": \"dog\"}, \"ownerId\": 1}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    public void testAddPetWithoutName_ShouldFail() {
        String petJson = "{\"name\": \"\", \"birthDate\": \"2024-01-15\", \"type\": {\"id\": 1, \"name\": \"dog\"}, \"ownerId\": 1}";
        //no name provided
        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }
}