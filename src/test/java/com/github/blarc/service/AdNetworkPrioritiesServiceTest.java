package com.github.blarc.service;

import com.github.blarc.model.AdNetworkPriorities;
import com.github.blarc.model.AdTypeEnum;
import com.github.blarc.model.PlatformEnum;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class AdNetworkPrioritiesServiceTest {

    private static final Map<String, AdNetworkPriorities> testAdNetworkPrioritiesMap = Map.of(
            "AA", new AdNetworkPriorities(
                    List.of("AdMob", "AppLovin", "Meta Audience Network"),
                    List.of("AdMob", "Vungle", "Chartboost"),
                    List.of("Unity Ads", "IronSource", "AdColony")
            ),
            "BB", new AdNetworkPriorities(
                    List.of("AdMob", "InMobi", "Smaato"),
                    List.of("Fyber", "AdMob", "Pangle"),
                    List.of("Unity Ads", "AdMob", "Mintegral")
            ),
            "CC", new AdNetworkPriorities(
                    List.of("AdMob", "Tapjoy", "Yahoo Ad Tech"),
                    List.of("AdMob", "Liftoff", "DigitalTurbine"),
                    List.of("Unity Ads", "Moloco", "AdMob")
            ),
            "DD", new AdNetworkPriorities(
                    List.of("Verve", "AdMob", "Xandr"),
                    List.of("SmartAdServer", "AdMob", "Equativ"),
                    List.of("Unity Ads", "Ogury", "AdMob")
            )
    );

    @Inject
    AdNetworkPrioritiesService adNetworkPrioritiesService;

    @Inject
    RedisService redisService;

    @AfterEach
    void cleanupRedis() {
        for (String countryCode : testAdNetworkPrioritiesMap.keySet()) {
            redisService.delete(countryCode);
        }
    }


    @Test
    public void getNetworkPriorities_fallback() {
        Map<AdTypeEnum, List<String>> networkPrioritiesMap = adNetworkPrioritiesService.getNetworkPrioritiesMap(
                "AA",
                null,
                null
        );

        assertThat(networkPrioritiesMap.size()).isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.BANNER)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.INTERSTITIAL)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.REWARDED)).size().isEqualTo(3);
    }

    @Test
    public void getNetworkPriorities_fallback_android_filter() {
        Map<AdTypeEnum, List<String>> networkPrioritiesMap = adNetworkPrioritiesService.getNetworkPrioritiesMap(
                "AA",
                PlatformEnum.ANDROID,
                "9.3.5"
        );

        assertThat(networkPrioritiesMap.size()).isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.BANNER)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.BANNER)).doesNotContain("AdMob");
        assertThat(networkPrioritiesMap.get(AdTypeEnum.INTERSTITIAL)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.INTERSTITIAL)).doesNotContain("AdMob");
        assertThat(networkPrioritiesMap.get(AdTypeEnum.REWARDED)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.REWARDED)).doesNotContain("AdMob");
    }

    @Test
    public void updateAdNetworkPriorities() {
        adNetworkPrioritiesService.updateAdNetworkPriorities(testAdNetworkPrioritiesMap);
        Map<AdTypeEnum, List<String>> networkPrioritiesMap = adNetworkPrioritiesService.getNetworkPrioritiesMap(
                "AA",
                null,
                null
        );

        assertThat(networkPrioritiesMap.size()).isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.BANNER)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.INTERSTITIAL)).size().isEqualTo(3);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.REWARDED)).size().isEqualTo(4);
        assertThat(networkPrioritiesMap.get(AdTypeEnum.REWARDED)).contains("AdMob-OptOut");
    }
}
