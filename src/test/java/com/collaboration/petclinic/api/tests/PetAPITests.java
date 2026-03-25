package com.collaboration.petclinic.api.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PetAPITests {

    @Test
    public void testAddPetSuccessfully() {
        RestAssured.baseURI = "http://localhost:9966/petclinic";

        String petJson = "{"
                + "\"name\": \"Bella\","
                + "\"birthDate\": \"2024-01-15\","
                + "\"type\": { \"id\": 1, \"name\": \"dog\" },"
                + "\"ownerId\": 1"
                + "}";

        given()
                .contentType(ContentType.JSON)
                .body(petJson)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(201)
                .body("name", equalTo("Bella"));
    }
}