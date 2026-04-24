package com.collaboration.petclinic.api.tests;

import com.collaboration.petclinic.api.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerAPITest extends BaseTest {

  private static final String OWNERS_PATH = "/owners";
  private static final int NON_EXISTING_OWNER_ID = 999999;

  // Shared owner ID created once before all tests
  private int sharedOwnerId;

  @BeforeAll
  void setup() {
    sharedOwnerId = createOwner();
  }

  private String ownerPath(int ownerId) {
    return OWNERS_PATH + "/" + ownerId;
  }

  private String ownerPayload(String firstName, String lastName, String address, String city, String telephone) {
    return """
        {
          "firstName": "%s",
          "lastName": "%s",
          "address": "%s",
          "city": "%s",
          "telephone": "%s"
        }
        """.formatted(firstName, lastName, address, city, telephone);
  }

  private String validOwnerPayload() {
    return ownerPayload("Test", "User", "Street 1", "Skopje", "1234567890");
  }

  private io.restassured.response.ValidatableResponse getOwners() {
    return RestAssured.given()
            .when()
            .get(OWNERS_PATH)
            .then();
  }

  private io.restassured.response.ValidatableResponse getOwner(int ownerId) {
    return RestAssured.given()
            .when()
            .get(ownerPath(ownerId))
            .then();
  }

  private io.restassured.response.ValidatableResponse postOwner(String payload) {
    return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post(OWNERS_PATH)
            .then();
  }

  private io.restassured.response.ValidatableResponse putOwner(int ownerId, String payload) {
    return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .put(ownerPath(ownerId))
            .then();
  }

  private io.restassured.response.ValidatableResponse deleteOwner(int ownerId) {
    return RestAssured.given()
            .when()
            .delete(ownerPath(ownerId))
            .then();
  }

  private int createOwner() {
    return postOwner(validOwnerPayload())
            .statusCode(201)
            .extract()
            .path("id");
  }

  // ---------------- GET /owners ----------------

  @Test // TC01
  void shouldGetAllOwners() {
    // Uses sharedOwnerId created in @BeforeAll — no extra API call needed
    getOwners()
            .statusCode(200)
            .body("$", not(empty()));
  }
  // TC02 removed — was identical to TC01 with no additional value
  // ---------------- GET /owners/{id} ----------------

  @Test // TC03
  void shouldGetOwnerByValidId() {
    // Reuses sharedOwnerId instead of creating a new owner
    getOwner(sharedOwnerId)
            .statusCode(200)
            .body("id", equalTo(sharedOwnerId));
  }

  @Test // TC04
  void shouldReturn404ForInvalidOwnerId() {
    getOwner(NON_EXISTING_OWNER_ID)
            .statusCode(404);
  }

  // ---------------- POST /owners ----------------

  @Test // TC05
  void shouldCreateOwnerWithValidData() {
    postOwner(ownerPayload("John", "Doe", "Street 1", "Skopje", "1234567890"))
            .statusCode(201)
            .body("id", notNullValue());
  }

  @Test // TC06
  void shouldFailWhenMissingRequiredFields() {
    postOwner("""
        {
          "firstName": "John"
        }
        """)
            .statusCode(400);
  }

  @Test // TC07
  void shouldFailWithInvalidData() {
    postOwner(ownerPayload("", "", "", "", "abc"))
            .statusCode(400);
  }

  @Test // TC08
  void shouldFailWhenCreatingDuplicateOwner() {
    String body = ownerPayload("Duplicate", "User", "Street 2", "Skopje", "9876543210");

    postOwner(body)
            .statusCode(201);

    postOwner(body)
            .statusCode(anyOf(is(400), is(409)));
  }

  // ---------------- PUT /owners/{id} ----------------

  @Test // TC09
  void shouldUpdateOwnerWithValidData() {
    putOwner(sharedOwnerId, ownerPayload("Updated", "User", "New Address", "Bitola", "0111222333"))
            .statusCode(anyOf(is(200), is(204)));
  }

  @Test // TC10
  void shouldReturn404WhenUpdatingNonExistingOwner() {
    putOwner(NON_EXISTING_OWNER_ID, ownerPayload("Test", "User", "Address", "City", "1234567890"))
            .statusCode(404);
  }

  @Test // TC11
  void shouldFailUpdateWithInvalidData() {
    putOwner(sharedOwnerId, ownerPayload("", "", "", "", "abc"))
            .statusCode(400);
  }

  @Test // TC12
  void shouldFailUpdateWithEmptyBody() {
    putOwner(sharedOwnerId, "{}")
            .statusCode(400);
  }

  // ---------------- DELETE /owners/{id} ----------------

  @Test // TC13
  void shouldDeleteExistingOwner() {
    int ownerToDelete = createOwner();
    deleteOwner(ownerToDelete)
            .statusCode(anyOf(is(200), is(204)));
  }

  @Test // TC14
  void shouldReturn404WhenDeletingNonExistingOwner() {
    deleteOwner(NON_EXISTING_OWNER_ID)
            .statusCode(404);
  }
}