package com.collaboration.petclinic.api.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;

public class BaseTest {

    public static RequestSpecification requestSpec;

    @BeforeClass
    public static void createRequestSpecification() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost:9966/petclinic/api")
                .setContentType(ContentType.JSON).build();
        RestAssured.port = 9966;
    }

}