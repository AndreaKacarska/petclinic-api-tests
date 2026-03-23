package com.collaboration.petclinic.api.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;

import junitparams.Parameters;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;

@RunWith(JUnitParamsRunner.class)
public class VisitTest extends BaseTest {

    @Test
    public void testGetVisits() {
        given()
                    .spec(requestSpec)
                .when()
                    .get( "/visits")
                .then()
                    .statusCode(200)
                    .body("size()", greaterThan(0));
    }


    public Object[] visits() {
        return new Object[]{
                new Object [] {"2026-06-07", "Valid visit", 7, 201},
                new Object []{"2026-01-04", "Visit with past date", 7, 201},
                new Object []{"2026-06-07", "Visit with invalid desc 1234 ? !", 7, 201},
                new Object []{"2026-01-04", null, 7, 400},
                new Object []{"2026-01-04", "Non existent pet", 33, 400}
        };
    }


    // status code for the 4-th and 5-th case was 204 even tho it was supposed to be 400 - bad request
    @Test
    @Parameters(method = "visits")
    public void testAddVisit(String date, String desc, int petId, int status_code) {
        String json = String.format("""
        {
            "date": "%s",
            "description": "%s",
            "petId": %d
        }
        """, date, desc, petId);

        given()
                    .spec(requestSpec)
                    .header("Content-Type", "application/json")
                    .body(json)
                .when()
                    .post("/visits")
                .then()
                    .assertThat()
                    .statusCode(status_code)
                    .body("description", desc != null ? equalTo(desc) : nullValue());
    }


    public Object[] editing_values() {
        return new Object[]{
                new Object [] {"2013-01-02", "rabies shot", 2, 304},
                new Object [] {"2026-01-04", "Valid edit", 11, 204},
                new Object [] {"2026-01-01", "Edit with past date", 1, 204},
                new Object [] {"2026-06-07", "Description!!??123", 6, 204},
                new Object []{null, "Without Date", 7, 400},
                new Object []{"2026-06-07", null, 7, 400},
                new Object []{"2026-01-04", "Valid visit", 50, 404}
        };
    }
    // according to Swagger documentation, if no fields are changed during an edit, the API should return status code 304 (Not Modified). However, the actual response was 204.
    // In the second test case, the expected status code was 204 since the visit with the given ID exists. However, the API returned 404 (Not Found).
    // When attempting to update a visit with an empty description, the expected response was 400 (Bad Request) due to invalid input. Instead, the API returned 204 (No Content),
    @Test
    @Parameters(method = "editing_values")
    public void testEditVisit(String date, String desc, int visitId, int status_code) {
        String json = String.format("""
        {
            "date": "%s",
            "description": "%s"
        }
        """, date, desc);
        given()
                    .spec(requestSpec)
                    .contentType("application/json")
                    .body(json)
                .when()
                    .put("/visits/" + visitId)
                .then()
                    .assertThat()
                    .statusCode(status_code);
    }




    @Test
    public void testDeleteValidVisit() {
        given()
                    .spec(requestSpec)
                    .header("Content-Type", "application/json")
                .when()
                    .delete("/visits/8")
                .then()
                    .assertThat()
                    .statusCode(204);

    }
    @Test
    public void testDeleteInvalidVisit() {
        given()
                    .spec(requestSpec)
                    .header("Content-Type", "application/json")
                .when()
                    .delete("/visits/8")
                .then()
                    .assertThat()
                    .statusCode(404);

    }
}




