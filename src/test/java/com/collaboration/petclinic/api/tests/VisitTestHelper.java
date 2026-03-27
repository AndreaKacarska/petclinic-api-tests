package com.collaboration.petclinic.api.tests;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.*;

public class VisitTestHelper {

    public static String buildVisitJson(String date, String description, int petId) {
        return String.format("""
        {
            "date": "%s",
            "description": "%s",
            "petId": %d
        }
        """, date, description, petId);
    }

    public static String buildVisitUpdateJson(String date, String description) {
        return String.format("""
        {
            "date": "%s",
            "description": "%s"
        }
        """, date, description);
    }

    public static Response createVisit(RequestSpecification requestSpec, String date, String description, int petId) {
        return given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(buildVisitJson(date, description, petId))
            .when()
                .post("/visits");
    }


    public static Response getVisitById(RequestSpecification requestSpec, int visitId) {
        return given()
                .spec(requestSpec)
            .when()
                .get("/visits/" + visitId);
    }

    public static Response updateVisit(RequestSpecification requestSpec, int visitId, String date, String description) {
        return given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(buildVisitUpdateJson(date, description))
            .when()
                .put("/visits/" + visitId);
    }

    public static Response deleteVisit(RequestSpecification requestSpec, int visitId) {
        return given()
                .spec(requestSpec)
            .when()
                .delete("/visits/" + visitId);
    }

    public static int createVisitAndAssert(RequestSpecification requestSpec, String date, String description, int petId) {
        Response response = createVisit(requestSpec, date, description, petId);
        response.then()
            .assertThat()
            .statusCode(201);
        return response.jsonPath().getInt("id");
    }

    public static void verifyVisitStructure(Response response) {
        response.then()
            .assertThat()
            .body("$", org.hamcrest.Matchers.hasKey("id"))
            .body("$", org.hamcrest.Matchers.hasKey("date"))
            .body("$", org.hamcrest.Matchers.hasKey("description"))
            .body("$", org.hamcrest.Matchers.hasKey("petId"));
    }

    public static void verifyVisitData(Response response, String expectedDate, String expectedDesc, int expectedPetId) {
        response.then()
            .assertThat()
            .body("date", org.hamcrest.CoreMatchers.equalTo(expectedDate))
            .body("description", org.hamcrest.CoreMatchers.equalTo(expectedDesc))
            .body("petId", org.hamcrest.CoreMatchers.equalTo(expectedPetId));
    }
}

