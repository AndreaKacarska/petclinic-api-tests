package com.collaboration.petclinic.api.tests;

import com.collaboration.petclinic.api.base.BaseTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

class OwnerAPITest extends BaseTest {

  @Test
  void shouldReturnOwnersList() {
    RestAssured.given()
      .when()
      .get("/owners")
      .then()
      .statusCode(200);
  }

}
