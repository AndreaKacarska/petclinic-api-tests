package com.collaboration.petclinic.api.tests;

import com.collaboration.petclinic.api.base.BaseTest;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;

import junitparams.Parameters;
import org.junit.After;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@RunWith(JUnitParamsRunner.class)
public class VisitTest extends BaseTest {

    private List<Integer> createdVisits = new ArrayList<>();

    @Test
    public void testGetVisits_Success() {
        var response = given()
                .spec(requestSpec)
                .get("/visits");

        response.then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .body("[0]", hasKey("date"))
                .body("[0]", hasKey("description"))
                .body("[0]", hasKey("petId"));
    }

    @Test
    public void testGetVisits_VerifyResponseStructure() {
        given()
                .spec(requestSpec)
                .get("/visits")
                .then()
                .statusCode(200)
                .body("[0].id", notNullValue())
                .body("[0].id", greaterThan(0))
                .body("[0].date", matchesPattern("\\d{4}-\\d{2}-\\d{2}"))
                .body("[0].petId", greaterThan(0));
    }

    public Object[] addVisit_ValidCases() {
        return new Object[]{
                // date, description, petId, expectedStatus
                new Object [] {"2026-06-07", "Valid visit", 7},
                new Object [] {"2026-01-04", "Visit with past date", 7},
                new Object [] {"2026-06-07", "Visit with special chars !@#$%", 7},
                new Object [] {"2026-12-31", "New year visit", 1}
        };
    }

    public Object[] addVisit_InvalidCases() {
        return new Object[]{
                // date, description, petId, expectedStatus, description
                new Object [] {null, "Missing date", 7, 400},
                new Object [] {"2026-01-04", null, 7, 400},
                new Object [] {"2026-01-04", "Invalid pet", 99999, 404},
                new Object [] {"", "Empty date", 7, 400},
                new Object [] {"2026-02-30", "Invalid day", 7, 400}
        };
    }

    @Test
    @Parameters(method = "addVisit_ValidCases")
    public void testAddVisit_Success(String date, String desc, int petId) {
        Response createResponse = VisitTestHelper.createVisitAndAssert(requestSpec, date, desc, petId);
        int visitId = createResponse.jsonPath().getInt("id");
        createdVisits.add(visitId);
        VisitTestHelper.verifyVisitData(createResponse, date, desc, petId);
        VisitTestHelper.verifyVisitStructure(createResponse);
    }

    // returns 500 instead of 400 for null date and description, but we will accept it as a valid response for now since it indicates a server error due to invalid input
    @Test
    @Parameters(method = "addVisit_InvalidCases")
    public void testAddVisit_InvalidInput(String date, String desc, int petId, int expectedStatus) {
        Response response = VisitTestHelper.createVisit(requestSpec, date, desc, petId);
        response.then().assertThat().statusCode(expectedStatus);
    }

    public Object[] editVisit_ValidCases() {
        return new Object[]{
                // date, description, visitId, expectedStatus
                new Object [] {"2026-01-04", "Valid edit", 1, 204},
                new Object [] {"2026-01-01", "Edit with past date", 1, 204},
                new Object [] {"2026-06-07", "Description!!??123", 1, 204}
        };
    }

    public Object[] editVisit_InvalidCases() {
        return new Object[]{
                // date, description, visitId, expectedStatus, testCase
                new Object [] {null, "Without Date", 7, 400},
                new Object [] {"2026-06-07", null, 7, 400},
                new Object [] {"2026-01-04", "Valid visit", 99999, 404},
                new Object [] {"invalid-date", "Bad format", 1, 400},
                new Object [] {"2013-01-02", "rabies shot", 1, 200}  // This was incorrectly expecting 304
        };
    }

    @Test
    @Parameters(method = "editVisit_ValidCases")
    public void testEditVisit_Success(String date, String desc, int visitId, int expectedStatus) {
        Response response = VisitTestHelper.updateVisit(requestSpec, visitId, date, desc);
        response.then().statusCode(expectedStatus);

        if (expectedStatus == 204 || response.getBody().asString().isEmpty()) return;

        int petId = response.jsonPath().getInt("petId");

        VisitTestHelper.verifyVisitStructure(response);
        VisitTestHelper.verifyVisitData(response, date, desc, petId);
    }

    // returns 500 instead of 400 for null date and description, but we will accept it as a valid response for now since it indicates a server error due to invalid input
    @Test
    @Parameters(method = "editVisit_InvalidCases")
    public void testEditVisit_InvalidInput(String date, String desc, int visitId, int expectedStatus) {
        VisitTestHelper.updateVisit(requestSpec, visitId, date, desc)
                .then()
                .statusCode(expectedStatus);
    }

    @Test
    public void testDeleteValidVisit() {
        VisitTestHelper.deleteVisit(requestSpec, 1)
                .then()
                .statusCode(204);
    }

    @Test
    public void testDeleteInvalidVisit() {
        VisitTestHelper.deleteVisit(requestSpec, 99999)
                .then()
                .statusCode(404);
    }

    @After
    public void tearDown() {
        for (int id : createdVisits) {
            VisitTestHelper.deleteVisit(requestSpec, id);
        }
        createdVisits.clear();
    }
}
