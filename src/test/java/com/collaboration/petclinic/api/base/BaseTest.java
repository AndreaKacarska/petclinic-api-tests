package com.collaboration.petclinic.api.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;                  // JUnit4
import org.junit.jupiter.api.BeforeAll;        // JUnit5

public class BaseTest {

  protected static RequestSpecification requestSpec;

  // JUnit4 lifecycle
  @BeforeClass
  public static void initForJUnit4() {
    init();
  }

  @BeforeAll
  public static void initForJUnit5() {
    init();
  }

  private static void init() {
    if (requestSpec != null) {
      return;
    }

    requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost:9966/petclinic/api")
            .setContentType(ContentType.JSON).build();

    RestAssured.port = 9966;
     RestAssured.baseURI = "http://localhost";
     RestAssured.basePath = "/petclinic/api";
  }
}