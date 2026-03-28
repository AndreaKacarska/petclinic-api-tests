package com.collaboration.petclinic.api.tests;

import com.collaboration.petclinic.api.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class OwnerAPITest extends BaseTest {

  // 🔹 Helper method to create owner and return ID
  private int createOwner() {
    return RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "Test",
              "lastName": "User",
              "address": "Street 1",
              "city": "Skopje",
              "telephone": "1234567890"
            }
        """)
            .when()
            .post("/owners")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
  }

  // ---------------- GET /owners ----------------

  @Test // TC01
  void shouldGetAllOwners() {
    createOwner(); // ensure at least one exists

    RestAssured.given()
            .when()
            .get("/owners")
            .then()
            .statusCode(200)
            .body("$", not(empty()));
  }

  @Test // TC02
  void shouldReturnOwnersList() {
    RestAssured.given()
            .when()
            .get("/owners")
            .then()
            .statusCode(200);
  }

  // ---------------- GET /owners/{id} ----------------

  @Test // TC03
  void shouldGetOwnerByValidId() {
    int ownerId = createOwner();

    RestAssured.given()
            .when()
            .get("/owners/" + ownerId)
            .then()
            .statusCode(200)
            .body("id", equalTo(ownerId));
  }

  @Test // TC04
  void shouldReturn404ForInvalidOwnerId() {
    RestAssured.given()
            .when()
            .get("/owners/999999")
            .then()
            .statusCode(404);
  }

  // ---------------- POST /owners ----------------

  @Test // TC05
  void shouldCreateOwnerWithValidData() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "John",
              "lastName": "Doe",
              "address": "Street 1",
              "city": "Skopje",
              "telephone": "1234567890"
            }
        """)
            .when()
            .post("/owners")
            .then()
            .statusCode(201)
            .body("id", notNullValue());
  }

  @Test // TC06
  void shouldFailWhenMissingRequiredFields() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "John"
            }
        """)
            .when()
            .post("/owners")
            .then()
            .statusCode(400);
  }

  @Test // TC07
  void shouldFailWithInvalidData() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "",
              "lastName": "",
              "address": "",
              "city": "",
              "telephone": "abc"
            }
        """)
            .when()
            .post("/owners")
            .then()
            .statusCode(400);
  }

  @Test // TC08
  void shouldFailWhenCreatingDuplicateOwner() {
    String body = """
        {
          "firstName": "Duplicate",
          "lastName": "User",
          "address": "Street 2",
          "city": "Skopje",
          "telephone": "9876543210"
        }
        """;

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/owners")
            .then()
            .statusCode(201);

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/owners")
            .then()
            .statusCode(anyOf(is(400), is(409)));
  }

  // ---------------- PUT /owners/{id} ----------------

  @Test // TC09
  void shouldUpdateOwnerWithValidData() {
    int ownerId = createOwner();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "Updated",
              "lastName": "User",
              "address": "New Address",
              "city": "Bitola",
              "telephone": "0111222333"
            }
        """)
            .when()
            .put("/owners/" + ownerId)
            .then()
            .statusCode(anyOf(is(200), is(204)));
  }

  @Test // TC10
  void shouldReturn404WhenUpdatingNonExistingOwner() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "Test",
              "lastName": "User",
              "address": "Address",
              "city": "City",
              "telephone": "1234567890"
            }
        """)
            .when()
            .put("/owners/999999")
            .then()
            .statusCode(404);
  }

  @Test // TC11
  void shouldFailUpdateWithInvalidData() {
    int ownerId = createOwner();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
            {
              "firstName": "",
              "lastName": "",
              "address": "",
              "city": "",
              "telephone": "abc"
            }
        """)
            .when()
            .put("/owners/" + ownerId)
            .then()
            .statusCode(400);
  }

  @Test // TC12
  void shouldFailUpdateWithEmptyBody() {
    int ownerId = createOwner();

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
            .put("/owners/" + ownerId)
            .then()
            .statusCode(400);
  }

  // ---------------- DELETE /owners/{id} ----------------

  @Test // TC13
  void shouldDeleteExistingOwner() {
    int ownerId = createOwner();

    RestAssured.given()
            .when()
            .delete("/owners/" + ownerId)
            .then()
            .statusCode(anyOf(is(200), is(204)));
  }

  @Test // TC14
  void shouldReturn404WhenDeletingNonExistingOwner() {
    RestAssured.given()
            .when()
            .delete("/owners/999999")
            .then()
            .statusCode(404);
  }
}