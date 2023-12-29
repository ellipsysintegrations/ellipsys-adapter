package hu.ellipsysintegrations.adapter.allure.server.api.client;

import hu.ellipsysintegrations.adapter.allure.server.api.dto.SendResultsRequestDto;
import hu.ellipsysintegrations.adapter.allure.server.properties.AllureServerAdapterProperties;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Getter
@Component
@Accessors(fluent = true)
public class AllureServerClient {

    @Getter(AccessLevel.PROTECTED)
    @Autowired
    private AllureServerAdapterProperties properties;

    public String baseUrl() {
        return String.format("%s://%s:%d/allure-docker-service", properties.protocol(), properties.host(), properties.mappedPort());
    }

    public Map<String, String> headers() {
        Map<String, String> requestHeader = new HashMap<>();
        requestHeader.put(Header.ACCEPT, Header.ANY);
        requestHeader.put(Header.CONNECTION, Header.KEEP_ALIVE);
        requestHeader.put(Header.HOST, properties.host());
        requestHeader.put(Header.REFERER, String.format("%s://%s:%d", properties.protocol(), properties.host(), properties.mappedPort()));
        return requestHeader;
    }

    public Response postSendResults(String projectId, SendResultsRequestDto requestDto) {
        Map<String, String> requestHeader = headers();
        requestHeader.put(Header.ACCEPT, Header.APPLICATION_JSON);
        requestHeader.put(Header.CONTENT_TYPE, Header.APPLICATION_JSON);

        return RestAssured
                .given()
                .log().ifValidationFails()
                .queryParam(QueryParam.PROJECT_ID, projectId)
                .queryParam(QueryParam.FORCE_PROJECT_CREATION, QueryParam.TRUE)
                .headers(requestHeader)
                .body(requestDto.asJsonString())
                .when()
                .post(baseUrl() + Path.SEND_RESULTS)
                .then()
                .log().ifValidationFails()
                .spec(new ResponseSpecBuilder()
                        .expectContentType(MediaType.APPLICATION_JSON_VALUE)
                        .expectStatusCode(Matchers.anyOf(Matchers.is(OK.value()), Matchers.is(ACCEPTED.value()), Matchers.is(CREATED.value())))
                        .build())
                .extract().response();
    }

    public Response getGenerateReport(String projectId) {
        return RestAssured
                .given()
                .log().ifValidationFails()
                .queryParam(QueryParam.PROJECT_ID, projectId)
                .headers(headers())
                .when()
                .get(baseUrl() + Path.GENERATE_REPORT)
                .then()
                .log().ifValidationFails()
                .spec(new ResponseSpecBuilder()
                        .expectContentType(MediaType.APPLICATION_JSON_VALUE)
                        .expectStatusCode(Matchers.anyOf(Matchers.is(OK.value()), Matchers.is(ACCEPTED.value()), Matchers.is(CREATED.value())))
                        .build())
                .extract().response();
    }

    public Response getCleanResults(String projectId) {
        return RestAssured
                .given()
                .log().ifValidationFails()
                .queryParam(QueryParam.PROJECT_ID, projectId)
                .headers(headers())
                .when()
                .get(baseUrl() + Path.CLEAN_RESULTS)
                .then()
                .log().ifValidationFails()
                .spec(new ResponseSpecBuilder()
                        .expectContentType(MediaType.APPLICATION_JSON_VALUE)
                        .expectStatusCode(Matchers.anyOf(Matchers.is(OK.value()), Matchers.is(ACCEPTED.value()), Matchers.is(CREATED.value())))
                        .build())
                .extract().response();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Path {
        public static final String SEND_RESULTS = "/send-results";
        public static final String GENERATE_REPORT = "/generate-report";
        public static final String CLEAN_RESULTS = "/clean-results";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Header {
        public static final String ACCEPT = "accept";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final String CONNECTION = "Connection";
        public static final String KEEP_ALIVE = "keep-alive";
        public static final String ANY = "*/*";
        private static final String HOST = "Host";
        private static final String REFERER = "Referer";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryParam {
        public static final String FORCE_PROJECT_CREATION = "force_project_creation";
        public static final String TRUE = "true";
        private static final String PROJECT_ID = "project_id";
    }

}
