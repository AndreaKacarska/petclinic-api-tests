package com.collaboration.petclinic.api.tests;

import com.collaboration.petclinic.api.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class OwnerAPITestWithAI extends BaseTest {

  private Map<String, Object> validOwnerPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("firstName", "John");
    payload.put("lastName", "Doe");
    payload.put("address", "Street 1");
    payload.put("city", "Skopje");
    payload.put("telephone", "1234567890");
    return payload;
  }

  private Map<String, Object> ownerPayloadWithOverride(String field, Object value) {
    Map<String, Object> payload = validOwnerPayload();
    payload.put(field, value);
    return payload;
  }

  private Map<String, Object> ownerPayloadWithout(String field) {
    Map<String, Object> payload = validOwnerPayload();
    payload.remove(field);
    return payload;
  }

  private Response postOwner(Map<String, Object> payload) {
    return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/owners");
  }

  private int ownerCount() {
    return RestAssured.given()
            .when()
            .get("/owners")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("$")
            .size();
  }

  private static Stream<String> requiredFields() {
    return Stream.of("firstName", "lastName", "address", "city", "telephone");
  }

  private static Stream<Arguments> requiredFieldsWithBlankValue() {
    return Stream.of(
            Arguments.of("firstName", ""),
            Arguments.of("firstName", "   "),
            Arguments.of("lastName", ""),
            Arguments.of("lastName", "   "),
            Arguments.of("address", ""),
            Arguments.of("address", "   "),
            Arguments.of("city", ""),
            Arguments.of("city", "   "),
            Arguments.of("telephone", ""),
            Arguments.of("telephone", "   ")
    );
  }

  private static Stream<String> invalidNameValues() {
    return Stream.of("John1", "J@hn", "John Doe", "Anne-Marie", "O'Neil", "123");
  }

  private static Stream<String> validAddressValues() {
    return Stream.of(
            "Main St. #10/B-2",
            "Boulevard 8, Entrance C",
            "No. 15 @ Center!"
    );
  }

  private static Stream<String> validTelephoneValues() {
    return Stream.of("1234567890", "0000000000", "9876543210");
  }

  private static Stream<String> invalidTelephoneValues() {
    return Stream.of(
            "12345abcde",
            "123-456-7890",
            "123 456 7890",
            "+1234567890",
            "(123)4567890"
    );
  }

  @Test
  void shouldCreateOwnerWithValidData() {
    Map<String, Object> payload = validOwnerPayload();

    Response response = postOwner(payload);

    int createdId = response.then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("id", greaterThan(0))
            .body("firstName", equalTo(payload.get("firstName")))
            .body("lastName", equalTo(payload.get("lastName")))
            .body("address", equalTo(payload.get("address")))
            .body("city", equalTo(payload.get("city")))
            .body("telephone", equalTo(payload.get("telephone")))
            .extract()
            .path("id");

    RestAssured.given()
            .when()
            .get("/owners/" + createdId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdId))
            .body("firstName", equalTo(payload.get("firstName")))
            .body("lastName", equalTo(payload.get("lastName")))
            .body("address", equalTo(payload.get("address")))
            .body("city", equalTo(payload.get("city")))
            .body("telephone", equalTo(payload.get("telephone")));
  }

  @ParameterizedTest(name = "shouldRejectWhenRequiredFieldIsMissing: {0}")
  @MethodSource("requiredFields")
  void shouldRejectWhenRequiredFieldIsMissing(String field) {
    Map<String, Object> payload = ownerPayloadWithout(field);
    postOwner(payload)
            .then()
            .statusCode(400);
  }

  @ParameterizedTest(name = "shouldRejectWhenRequiredFieldIsNull: {0}")
  @MethodSource("requiredFields")
  void shouldRejectWhenRequiredFieldIsNull(String field) {
    Map<String, Object> payload = ownerPayloadWithOverride(field, null);
    postOwner(payload)
            .then()
            .statusCode(400);
  }

  @ParameterizedTest(name = "shouldRejectWhenRequiredFieldIsBlankOrWhitespace: {0}={1}")
  @MethodSource("requiredFieldsWithBlankValue")
  void shouldRejectWhenRequiredFieldIsBlankOrWhitespace(String field, String value) {
    postOwner(ownerPayloadWithOverride(field, value))
            .then()
            .statusCode(400);
  }

  @ParameterizedTest(name = "shouldRejectWhenFirstNameIsNotLettersOnly: {0}")
  @MethodSource("invalidNameValues")
  void shouldRejectWhenFirstNameIsNotLettersOnly(String firstName) {
    postOwner(ownerPayloadWithOverride("firstName", firstName))
            .then()
            .statusCode(400);
  }

  @ParameterizedTest(name = "shouldRejectWhenLastNameIsNotLettersOnly: {0}")
  @MethodSource("invalidNameValues")
  void shouldRejectWhenLastNameIsNotLettersOnly(String lastName) {
    postOwner(ownerPayloadWithOverride("lastName", lastName))
            .then()
            .statusCode(400);
  }

  @Test
  void shouldAcceptNamesWithLettersOnly() {
    Map<String, Object> payload = validOwnerPayload();
    payload.put("firstName", "Alice");
    payload.put("lastName", "Brown");

    postOwner(payload)
            .then()
            .statusCode(201)
            .body("id", notNullValue());
  }

  @ParameterizedTest(name = "shouldAcceptAddressWithAllowedCharacters: {0}")
  @MethodSource("validAddressValues")
  void shouldAcceptAddressWithAllowedCharacters(String address) {
    postOwner(ownerPayloadWithOverride("address", address))
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("address", equalTo(address));
  }

  @ParameterizedTest(name = "shouldAcceptWhenTelephoneContainsOnlyDigits: {0}")
  @MethodSource("validTelephoneValues")
  void shouldAcceptWhenTelephoneContainsOnlyDigits(String telephone) {
    postOwner(ownerPayloadWithOverride("telephone", telephone))
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("telephone", equalTo(telephone));
  }

  @ParameterizedTest(name = "shouldRejectWhenTelephoneContainsNonNumericCharacters: {0}")
  @MethodSource("invalidTelephoneValues")
  void shouldRejectWhenTelephoneContainsNonNumericCharacters(String telephone) {
    postOwner(ownerPayloadWithOverride("telephone", telephone))
            .then()
            .statusCode(400);
  }

  @Test
  void shouldRejectWhenTelephoneIsNull() {
    postOwner(ownerPayloadWithOverride("telephone", null))
            .then()
            .statusCode(400);
  }

  @Test
  void shouldHandleDuplicateCreateRequestConsistently() {
    Map<String, Object> payload = ownerPayloadWithOverride("firstName", "Duplicate");
    payload.put("lastName", "Owner");
    payload.put("telephone", "1122334455");

    postOwner(payload)
            .then()
            .statusCode(201)
            .body("id", notNullValue());

    postOwner(payload)
            .then()
            .statusCode(anyOf(is(400), is(409)));
  }

  @Test
  void shouldReturnJsonWhenOwnerIsCreated() {
    postOwner(validOwnerPayload())
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("firstName", notNullValue())
            .body("lastName", notNullValue())
            .body("address", notNullValue())
            .body("city", notNullValue())
            .body("telephone", notNullValue());
  }

  @Test
  void shouldRejectWhenBodyIsEmptyObject() {
    postOwner(new LinkedHashMap<>())
            .then()
            .statusCode(400);
  }

  @Test
  void shouldRejectWhenBodyIsMissing() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .when()
            .post("/owners")
            .then()
            .statusCode(anyOf(is(400), is(415)));
  }

  @Test
  void shouldRejectWhenJsonBodyIsMalformed() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{")
            .when()
            .post("/owners")
            .then()
            .statusCode(anyOf(is(400), is(415)));
  }

  @Test
  void shouldRejectWhenContentTypeIsNotJson() {
    RestAssured.given()
            .contentType(ContentType.TEXT)
            .body("firstName=John&lastName=Doe")
            .when()
            .post("/owners")
            .then()
            .statusCode(anyOf(is(400), is(415)));
  }

  @Test
  void shouldNotCreateOwnerWhenValidationFails() {
    int beforeCreateCount = ownerCount();

    postOwner(ownerPayloadWithOverride("telephone", "abc123"))
            .then()
            .statusCode(400);

    int afterCreateCount = ownerCount();
    assertEquals(beforeCreateCount, afterCreateCount,
            "Owner count should stay unchanged for invalid create requests");
  }
}

