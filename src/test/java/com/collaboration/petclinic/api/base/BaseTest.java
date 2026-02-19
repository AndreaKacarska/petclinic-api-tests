package com.collaboration.petclinic.api.base;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {

  @BeforeAll
  static void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port=9966;
    RestAssured.basePath="/petclinic/api";
  }
}
