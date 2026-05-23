package com.example.incident.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class IncidentExplainResourceTest {

    @Test
    void getExplainReturns200ForKnownIncident() {
        given()
            .when().get("/api/incidents/inc-2026-0042/explain")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("incidentId", equalTo("inc-2026-0042"));
    }

    @Test
    void getExplainReturns404ForUnknownIncident() {
        given()
            .when().get("/api/incidents/unknown-999/explain")
            .then()
            .statusCode(404);
    }

    @Test
    void getExplainAiGeneratedIsFalseInDefaultConfig() {
        given()
            .when().get("/api/incidents/inc-2026-0042/explain")
            .then()
            .statusCode(200)
            .body("aiGenerated", equalTo(false))
            .body("provider", equalTo("noop"));
    }
}
