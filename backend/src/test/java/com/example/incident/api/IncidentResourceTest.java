package com.example.incident.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class IncidentResourceTest {

    @Test
    void getReportReturns200WithCorrectIncidentId() {
        given()
            .when().get("/api/incidents/inc-2026-0042")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("incidentId", equalTo("inc-2026-0042"));
    }

    @Test
    void getReportReturns404ForUnknownId() {
        given()
            .when().get("/api/incidents/unknown-999")
            .then()
            .statusCode(404);
    }

    @Test
    void getReportResponseHasAllRequiredTopLevelFields() {
        given()
            .when().get("/api/incidents/inc-2026-0042")
            .then()
            .statusCode(200)
            .body("incidentId", notNullValue())
            .body("title", notNullValue())
            .body("status", notNullValue())
            .body("reviewRequirement", notNullValue())
            .body("timeline", notNullValue())
            .body("evidence", notNullValue())
            .body("topFindings", notNullValue());
    }

    @Test
    void getMarkdownReportReturns200WithTextMarkdownContentType() {
        given()
            .when().get("/api/incidents/inc-2026-0042/report.md")
            .then()
            .statusCode(200)
            .contentType("text/markdown");
    }

    @Test
    void getMarkdownReportContainsRequiredSections() {
        given()
            .when().get("/api/incidents/inc-2026-0042/report.md")
            .then()
            .statusCode(200)
            .body(containsString("# Incident Report"))
            .body(containsString("## Timeline"))
            .body(containsString("## Top RCA Findings"));
    }
}
