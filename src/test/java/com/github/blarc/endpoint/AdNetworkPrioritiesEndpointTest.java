package com.github.blarc.endpoint;

import com.github.blarc.model.AdNetworkPriorities;
import com.github.blarc.service.RedisService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AdNetworkPrioritiesEndpointTest {

    @Inject
    RedisService redisService;

    @AfterEach
    void cleanupRedis() {
        redisService.delete("AA");
        redisService.delete("BB");
    }

    @Test
    public void getAdNetworkPriorities_fallback() {
        given()
                .pathParam("country_code", "AA")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("banner", hasSize(3))
                .body("interstitial", hasSize(3))
                .body("rewarded", hasSize(3));
    }

    @Test
    public void getAdNetworkPriorities_withPlatformFilter() {
        given()
                .pathParam("country_code", "AA")
                .queryParam("platform", "ANDROID")
                .queryParam("os_version", "9.0.0")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("banner", not(hasItem("AdMob")))
                .body("interstitial", not(hasItem("AdMob")))
                .body("rewarded", not(hasItem("AdMob")))
                .body("banner", hasItem("AdMob-OptOut"))
                .body("interstitial", hasItem("AdMob-OptOut"))
                .body("rewarded", hasItem("AdMob-OptOut"));
    }

    @Test
    public void getAdNetworkPriorities_chinaFilter() {
        given()
                .pathParam("country_code", "CN")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("banner", not(hasItem("Facebook")))
                .body("interstitial", not(hasItem("Facebook")))
                .body("rewarded", not(hasItem("Facebook")));
    }

    @Test
    public void getAdNetworkPriorities_invalidCountryCode_lowercase() {
        given()
                .pathParam("country_code", "us")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(400);
    }

    @Test
    public void getAdNetworkPriorities_invalidCountryCode_tooLong() {
        given()
                .pathParam("country_code", "USA")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(400);
    }

    @Test
    public void getAdNetworkPriorities_invalidCountryCode_tooShort() {
        given()
                .pathParam("country_code", "U")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(400);
    }

    @Test
    public void getAdNetworkPriorities_invalidCountryCode_withNumbers() {
        given()
                .pathParam("country_code", "U1")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"dashboard"})
    public void getAllAdNetworkPriorities_authorized() {
        given()
                .when()
                .get("/ads")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void getAllAdNetworkPriorities_unauthorized() {
        given()
                .when()
                .get("/ads")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"processing"})
    public void updateAdNetworkPriorities_authorized() {
        Map<String, AdNetworkPriorities> priorities = Map.of(
                "AA", new AdNetworkPriorities(
                        List.of("AdMob", "AppLovin", "Unity Ads"),
                        List.of("AdMob", "Vungle", "Chartboost"),
                        List.of("Unity Ads", "IronSource", "AdMob")
                ),
                "BB", new AdNetworkPriorities(
                        List.of("Verve", "AdMob", "Xandr"),
                        List.of("SmartAdServer", "AdMob", "Equativ"),
                        List.of("Unity Ads", "Ogury", "AdMob")
                )
        );

        given()
                .contentType(ContentType.JSON)
                .body(priorities)
                .when()
                .put("/ads")
                .then()
                .statusCode(204);

        // Verify the data was saved
        given()
                .pathParam("country_code", "AA")
                .when()
                .get("/ads/{country_code}")
                .then()
                .statusCode(200)
                .body("banner", hasItems("AdMob", "AppLovin", "Unity Ads"))
                .body("interstitial", hasItems("AdMob", "Vungle", "Chartboost"))
                .body("rewarded", hasItems("Unity Ads", "IronSource", "AdMob"));
    }

    @Test
    public void updateAdNetworkPriorities_unauthorized() {
        Map<String, AdNetworkPriorities> priorities = Map.of(
                "AA", new AdNetworkPriorities(
                        List.of("AdMob"),
                        List.of("AdMob"),
                        List.of("AdMob")
                )
        );

        given()
                .contentType(ContentType.JSON)
                .body(priorities)
                .when()
                .put("/ads")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"processing"})
    public void updateAdNetworkPriorities_invalidCountryCode() {
        Map<String, AdNetworkPriorities> priorities = Map.of(
                "USA", new AdNetworkPriorities(
                        List.of("AdMob"),
                        List.of("AdMob"),
                        List.of("AdMob")
                )
        );

        given()
                .contentType(ContentType.JSON)
                .body(priorities)
                .when()
                .put("/ads")
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"processing"})
    public void updateAdNetworkPriorities_invalidCountryCode_lowercase() {
        Map<String, AdNetworkPriorities> priorities = Map.of(
                "us", new AdNetworkPriorities(
                        List.of("AdMob"),
                        List.of("AdMob"),
                        List.of("AdMob")
                )
        );

        given()
                .contentType(ContentType.JSON)
                .body(priorities)
                .when()
                .put("/ads")
                .then()
                .statusCode(400);
    }
}