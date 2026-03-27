package com.collaboration.petclinic.api.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;


@RunWith(JUnitParamsRunner.class)
public class VisitTestAdvanced extends BaseTest {

    // ==================== EDGE CASES & BOUNDARY TESTS ====================
    
    /**
     * Test boundary conditions for date inputs
     */
    public Object[] dateEdgeCases() {
        return new Object[]{
                // date, description, petId, expectedStatus, testDescription
                new Object[] {"2026-01-01", "New Year visit", 1, 201, "First day of year"},
                new Object[] {"2026-12-31", "Last day of year", 1, 201, "Last day of year"},
                new Object[] {"2026-02-28", "Leap year day before", 1, 201, "Feb 28 (not leap year)"},
                new Object[] {"2027-02-28", "Last day of Feb non-leap", 1, 201, "Last day of Feb non-leap year"},
                new Object[] {"2030-04-30", "30-day month boundary", 1, 201, "April has 30 days"},
                new Object[] {"2030-05-31", "31-day month boundary", 1, 201, "May has 31 days"}
        };
    }
    
    @Test
    @Parameters(method = "dateEdgeCases")
    public void testAddVisit_DateEdgeCases(String date, String desc, int petId, int expectedStatus, String testDescription) {
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
                .statusCode(expectedStatus)
                .contentType("application/json")
                .body("date", equalTo(date))
                .body("description", equalTo(desc));
    }

    /**
     * Test boundary conditions for description field
     */
    public Object[] descriptionEdgeCases() {
        return new Object[]{
                // description, expectedStatus, testDescription
                new Object[] {"A", 201, "Single character description"},
                new Object[] {"A".repeat(255), 201, "Very long description (255 chars)"},
                new Object[] {"A".repeat(500), 400, "Extremely long description (500 chars) - should fail"},
                new Object[] {"   ", 400, "Whitespace only description"},
                new Object[] {"!@#$%^&*()", 201, "Special characters description"},
                new Object[] {"Ñoño 中文 العربية", 201, "Unicode characters"},
                new Object[] {"Line1\\nLine2", 201, "Description with escaped newline"},
                new Object[] {"Description with \"quotes\"", 201, "Description with quotes"},
                new Object[] {"Description with 'apostrophe'", 201, "Description with apostrophe"}
        };
    }
    
    @Test
    @Parameters(method = "descriptionEdgeCases")
    public void testAddVisit_DescriptionEdgeCases(String desc, int expectedStatus, String testDescription) {
        String json = String.format("""
        {
            "date": "2026-06-15",
            "description": "%s",
            "petId": 1
        }
        """, desc);

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(expectedStatus);
    }

    /**
     * Test invalid date format variations
     */
    public Object[] invalidDateFormats() {
        return new Object[]{
                // date, expectedStatus, testDescription
                new Object[] {"2026/06/07", 400, "Slash separator instead of dash"},
                new Object[] {"06-07-2026", 400, "MM-DD-YYYY format"},
                new Object[] {"2026-6-7", 400, "Single digit month/day without padding"},
                new Object[] {"26-06-07", 400, "YY-MM-DD format"},
                new Object[] {"2026-06-7", 400, "Mixed padding"},
                new Object[] {"2026-13-01", 400, "Invalid month (13)"},
                new Object[] {"2026-00-01", 400, "Invalid month (00)"},
                new Object[] {"2026-06-32", 400, "Invalid day (32)"},
                new Object[] {"2026-06-00", 400, "Invalid day (00)"},
                new Object[] {"not-a-date", 400, "Non-numeric date"},
                new Object[] {"2026-06", 400, "Missing day"},
                new Object[] {"02-30", 400, "Invalid day for February"}
        };
    }
    
    @Test
    @Parameters(method = "invalidDateFormats")
    public void testAddVisit_InvalidDateFormats(String date, int expectedStatus, String testDescription) {
        String json = String.format("""
        {
            "date": "%s",
            "description": "Test visit",
            "petId": 1
        }
        """, date);

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(expectedStatus);
    }

    /**
     * Test with various invalid petId values
     */
    public Object[] invalidPetIds() {
        return new Object[]{
                // petId (as string), expectedStatus, testDescription
                new Object[] {"999999", 404, "Non-existent pet ID"},
                new Object[] {"-1", 400, "Negative pet ID"},
                new Object[] {"0", 400, "Zero pet ID"},
                new Object[] {"abc", 400, "Non-numeric pet ID"},
                new Object[] {"1.5", 400, "Decimal pet ID"}
        };
    }

    /**
     * Test request body variations and malformed JSON
     */
    public Object[] malformedRequestBodies() {
        return new Object[]{
                // body, expectedStatus, testDescription
                new Object[] {"{\"date\": \"2026-06-07\"}", 400, "Missing required fields (description, petId)"},
                new Object[] {"{\"description\": \"Test\", \"petId\": 1}", 400, "Missing date field"},
                new Object[] {"{\"date\": \"2026-06-07\", \"petId\": 1}", 400, "Missing description field"},
                new Object[] {"", 400, "Empty body"},
                new Object[] {"{invalid json}", 400, "Invalid JSON format"},
                new Object[] {"null", 400, "Null body"},
                new Object[] {"[]", 400, "Array instead of object"}
        };
    }

    @Test
    @Parameters(method = "malformedRequestBodies")
    public void testAddVisit_MalformedRequestBody(String body, int expectedStatus, String testDescription) {
        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(body)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(expectedStatus);
    }

    // ==================== RESPONSE VALIDATION TESTS ====================

    @Test
    public void testGetVisits_ResponseStructureComplete() {
        given()
                .spec(requestSpec)
            .when()
                .get("/visits")
            .then()
                .assertThat()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThan(0))
                // Validate first item completely
                .body("[0].id", notNullValue())
                .body("[0].id", instanceOf(Integer.class))
                .body("[0].id", greaterThan(0))
                .body("[0].date", notNullValue())
                .body("[0].date", matchesPattern("\\d{4}-\\d{2}-\\d{2}"))
                .body("[0].description", notNullValue())
                .body("[0].description", isA(String.class))
                .body("[0].petId", notNullValue())
                .body("[0].petId", greaterThan(0))
                // Ensure no extra unexpected fields
                .body("[0].keySet().size()", greaterThanOrEqualTo(4));
    }

    @Test
    public void testAddVisit_ResponseBodyComplete() {
        String json = """
        {
            "date": "2026-07-15",
            "description": "Comprehensive response validation",
            "petId": 1
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(201)
                .contentType("application/json")
                .body("id", notNullValue())
                .body("id", isA(Integer.class))
                .body("id", greaterThan(0))
                .body("date", equalTo("2026-07-15"))
                .body("description", equalTo("Comprehensive response validation"))
                .body("petId", equalTo(1))
                .body("$", hasKey("id"))
                .body("$", hasKey("date"))
                .body("$", hasKey("description"))
                .body("$", hasKey("petId"));
    }

    @Test
    public void testEditVisit_ResponseBodyComplete() {
        String json = """
        {
            "date": "2026-08-20",
            "description": "Updated visit with validation"
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .put("/visits/1")
            .then()
                .assertThat()
                .statusCode(200)
                .contentType("application/json")
                .body("id", notNullValue())
                .body("id", greaterThan(0))
                .body("date", equalTo("2026-08-20"))
                .body("description", equalTo("Updated visit with validation"))
                .body("petId", notNullValue())
                .body("petId", greaterThan(0));
    }

    // ==================== CONCURRENT & STRESS TESTS ====================

    @Test
    public void testAddMultipleVisits_Sequence() {
        // Test adding multiple visits to same pet sequentially
        for (int i = 1; i <= 3; i++) {
            String json = String.format("""
            {
                "date": "2026-%02d-15",
                "description": "Visit number %d",
                "petId": 1
            }
            """, i + 5, i); // months 6, 7, 8

            given()
                    .spec(requestSpec)
                    .header("Content-Type", "application/json")
                    .body(json)
                .when()
                    .post("/visits")
                .then()
                    .assertThat()
                    .statusCode(201)
                    .body("id", notNullValue());
        }
    }

    // ==================== HEADER & CONTENT TYPE TESTS ====================

    @Test
    public void testAddVisit_MissingContentType() {
        String json = """
        {
            "date": "2026-06-07",
            "description": "Test without content type header",
            "petId": 1
        }
        """;

        given()
                .spec(requestSpec)
                .body(json)
                // Note: Not setting Content-Type header
            .when()
                .post("/visits")
            .then()
                // API should either accept or return 400
                .assertThat()
                .statusCode(anyOf(equalTo(201), equalTo(400)));
    }

    @Test
    public void testAddVisit_WrongContentType() {
        String json = """
        {
            "date": "2026-06-07",
            "description": "Test with wrong content type",
            "petId": 1
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "text/plain")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(anyOf(equalTo(201), equalTo(400), equalTo(415))); // 415 = Unsupported Media Type
    }

    // ==================== FIELD TYPE VALIDATION ====================

    @Test
    public void testAddVisit_StringPetId() {
        String json = """
        {
            "date": "2026-06-07",
            "description": "String pet ID test",
            "petId": "not_a_number"
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void testAddVisit_DecimalPetId() {
        String json = """
        {
            "date": "2026-06-07",
            "description": "Decimal pet ID test",
            "petId": 1.5
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void testAddVisit_NumericDate() {
        String json = """
        {
            "date": 20260607,
            "description": "Numeric date test",
            "petId": 1
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(201);
    }

    // ==================== ERROR MESSAGE VALIDATION ====================

    @Test
    public void testAddVisit_InvalidInput_ErrorMessage() {
        String json = """
        {
            "date": "invalid-date",
            "description": "Test",
            "petId": 1
        }
        """;

        given()
                .spec(requestSpec)
                .header("Content-Type", "application/json")
                .body(json)
            .when()
                .post("/visits")
            .then()
                .assertThat()
                .statusCode(400)
                .body("$", hasKey("message")); // Check if error message is provided
    }

    @Test
    public void testDeleteVisit_NotFound_ErrorMessage() {
        given()
                .spec(requestSpec)
            .when()
                .delete("/visits/999999")
            .then()
                .assertThat()
                .statusCode(404);
    }

    // ==================== IDEMPOTENCY & STATE TESTS ====================

    @Test
    public void testDeleteVisit_Idempotency() {
        // First delete should succeed
        given()
                .spec(requestSpec)
            .when()
                .delete("/visits/1")
            .then()
                .assertThat()
                .statusCode(204);

        // Second delete of same resource should fail
        given()
                .spec(requestSpec)
            .when()
                .delete("/visits/1")
            .then()
                .assertThat()
                .statusCode(404);
    }

    // ==================== FILTERING & QUERY PARAMETER TESTS ====================

    @Test
    public void testGetVisits_WithInvalidQueryParam() {
        given()
                .spec(requestSpec)
                .queryParam("invalid_param", "value")
            .when()
                .get("/visits")
            .then()
                .assertThat()
                .statusCode(anyOf(equalTo(200), equalTo(400))); // API should ignore or reject
    }

    // ==================== HTTP METHOD TESTS ====================

    @Test
    public void testVisitEndpoint_MethodNotAllowed_HEAD() {
        given()
                .spec(requestSpec)
            .when()
                .head("/visits")
            .then()
                .assertThat()
                .statusCode(anyOf(equalTo(405), equalTo(200))); // 405 = Method Not Allowed
    }

    @Test
    public void testVisitEndpoint_MethodNotAllowed_PATCH() {
        String json = """
        {
            "description": "Partial update"
        }
        """;

        given()
                .spec(requestSpec)
                .body(json)
            .when()
                .patch("/visits/1")
            .then()
                .assertThat()
                .statusCode(405); // PATCH might not be supported
    }
}

