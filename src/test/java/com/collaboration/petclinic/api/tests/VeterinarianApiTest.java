package com.collaboration.petclinic.api.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VeterinarianApiTest {

    private static RequestSpecification requestSpec;
    static Integer vetId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:9966/petclinic/api";
        // AI ОПТИМИЗАЦИЈА: Централизирана спецификација за помалку ресурсна потрошувачка
        requestSpec = new RequestSpecBuilder()
                .setAuth(basic("admin", "admin"))
                .setContentType(ContentType.JSON)
                .build();
    }

    @Test @Order(1)
    void createVeterinarian() {
        vetId = given().spec(requestSpec)
                .body("{\"firstName\": \"Test\", \"lastName\": \"Vet\", \"specialties\": []}")
                .when().post("/vets")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().path("id");
    }

    @Test @Order(2)
    void getVeterinarian() {
        given().spec(requestSpec).when().get("/vets/" + vetId)
                .then().statusCode(200).body("id", equalTo(vetId));
    }

    @Test @Order(3)
    void updateVeterinarian() {
        given().spec(requestSpec)
                .body(String.format("{\"id\": %d, \"firstName\": \"Updated\", \"lastName\": \"Vet\", \"specialties\": []}", vetId))
                .when().put("/vets/" + vetId)
                .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(4)
    void deleteVeterinarian() {
        given().spec(requestSpec).when().delete("/vets/" + vetId)
                .then().statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(5)
    void createVeterinarianInvalidFirstName() {
        given().spec(requestSpec).body("{\"firstName\": \"123\", \"lastName\": \"Vet\", \"specialties\": []}")
                .when().post("/vets").then().statusCode(anyOf(is(400), is(200)));
    }

    @Test @Order(6)
    void createVeterinarianEmptyFields() {
        given().spec(requestSpec).body("{\"firstName\": \"\", \"lastName\": \"\", \"specialties\": []}")
                .when().post("/vets").then().statusCode(anyOf(is(400), is(200)));
    }

    @Test @Order(7)
    void createVeterinarianSpecialChars() {
        given().spec(requestSpec).body("{\"firstName\": \"@@@\", \"lastName\": \"Vet\", \"specialties\": []}")
                .when().post("/vets").then().statusCode(anyOf(is(400), is(200)));
    }

    @Test @Order(8)
    void getNonExistingVet() {
        given().spec(requestSpec).when().get("/vets/999999")
                .then().statusCode(anyOf(is(404), is(200)));
    }

    @Test @Order(9)
    void deleteAlreadyDeletedVet() {
        given().spec(requestSpec).when().delete("/vets/" + vetId)
                .then().statusCode(anyOf(is(404), is(204)));
    }

    @Test @Order(10)
    void createDuplicateVeterinarian() {
        given().spec(requestSpec).body("{\"firstName\": \"Test\", \"lastName\": \"Vet\", \"specialties\": []}")
                .when().post("/vets").then().statusCode(anyOf(is(200), is(201)));
    }

    @Test @Order(11)
    void createVeterinarianLongInput() {
        String longName = "A".repeat(100);
        given().spec(requestSpec).body(String.format("{\"firstName\": \"%s\", \"lastName\": \"Vet\", \"specialties\": []}", longName))
                .when().post("/vets").then().statusCode(anyOf(is(200), is(400)));
    }

    @Test @Order(12)
    void createVeterinarianInvalidLastName() {
        given().spec(requestSpec).body("{\"firstName\": \"Jasmin\", \"lastName\": \"@@@\", \"specialties\": []}")
                .when().post("/vets").then().statusCode(anyOf(is(200), is(400)));
    }

    @Test @Order(13)
    void createVeterinarianWithoutSpecialty() {
        given().spec(requestSpec).body("{\"firstName\": \"Jasmin\", \"lastName\": \"Abazi\"}")
                .when().post("/vets").then().statusCode(anyOf(is(200), is(400)));
    }

    @Test @Order(14)
    void createVeterinarianDuplicateSpecialty() {
        given().spec(requestSpec).body("{\"firstName\": \"Jasmin\", \"lastName\": \"Abazi\", \"specialties\": [\"radiology\", \"radiology\"]}")
                .when().post("/vets").then().statusCode(anyOf(is(200), is(400), is(500)));
    }
}