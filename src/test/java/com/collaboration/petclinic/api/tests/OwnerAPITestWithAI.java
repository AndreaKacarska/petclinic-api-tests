package com.collaboration.petclinic.api.tests;

import com.collaboration.petclinic.api.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerAPITestWithAI extends BaseTest {

  private Map<String, Object> baseOwnerPayload;

  @BeforeAll
  void setupFixtures() {
    baseOwnerPayload = new LinkedHashMap<>();
    baseOwnerPayload.put("firstName", "John");
    baseOwnerPayload.put("lastName", "Doe");
    baseOwnerPayload.put("address", "Street 1");
    baseOwnerPayload.put("city", "Skopje");
    baseOwnerPayload.put("telephone", "1234567890");
  }

  private Map<String, Object> validOwnerPayload() {
    return new LinkedHashMap<>(baseOwnerPayload);
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

  private io.restassured.response.Response postOwner(Map<String, Object> payload) {
    return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/owners");
  }

  private static Stream<String> requiredFields() {
    return Stream.of("firstName", "lastName", "address", "city", "telephone");
  }

  private static Stream<String> invalidNameValues() {
    return Stream.of("John1", "J@hn", "123");
  }

  private static Stream<String> validAddressValues() {
    return Stream.of("Main St. #10/B-2");
  }

  private static Stream<String> validTelephoneValues() {
    return Stream.of("1234567890");
  }

  private static Stream<String> invalidTelephoneValues() {
    return Stream.of("12345abcde");
  }

  @Test
  void shouldCreateOwnerWithValidData() {
    Map<String, Object> payload = validOwnerPayload();

    postOwner(payload).then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("id", greaterThan(0))
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

  @ParameterizedTest(name = "shouldAcceptAddressWithAllowedCharacters: {0}")
  @MethodSource("validAddressValues")
  void shouldAcceptAddressWithAllowedCharacters(String address) {
    postOwner(ownerPayloadWithOverride("address", address))
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("address", equalTo(address));
  }

  @ParameterizedTest(name = "shouldRejectWhenTelephoneContainsNonNumericCharacters: {0}")
  @MethodSource("invalidTelephoneValues")
  void shouldRejectWhenTelephoneContainsNonNumericCharacters(String telephone) {
    postOwner(ownerPayloadWithOverride("telephone", telephone))
            .then()
            .statusCode(400);
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

  @Test
  void shouldRejectWhenBodyIsEmptyObject() {
    postOwner(new LinkedHashMap<>())
            .then()
            .statusCode(400);
  }

  @Test
  void shouldReturnErrorWhenBodyIsMissing() {
    RestAssured.given()
            .contentType(ContentType.JSON)
            .when()
            .post("/owners")
            .then()
            .statusCode(greaterThanOrEqualTo(400));
  }

  @Test
  void shouldReturnErrorWhenJsonBodyIsMalformed() {
    String malformedJson = "{";

    RestAssured.given()
            .contentType(ContentType.JSON)
            .body(malformedJson)
            .when()
            .post("/owners")
            .then()
            .statusCode(greaterThanOrEqualTo(400));
  }

  @Test
  void shouldReturnErrorWhenContentTypeIsNotJson() {
    RestAssured.given()
            .contentType(ContentType.TEXT)
            .body("firstName=John&lastName=Doe")
            .when()
            .post("/owners")
            .then()
            .statusCode(greaterThanOrEqualTo(400));
  }
}