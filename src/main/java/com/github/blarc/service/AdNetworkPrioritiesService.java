package com.github.blarc.service;

import com.github.blarc.model.AdNetworkPriorities;
import com.github.blarc.model.AdTypeEnum;
import com.github.blarc.model.PlatformEnum;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class AdNetworkPrioritiesService {

    @Inject
    RedisService redisService;

    // Fallback networks in case of failure
    private static final Map<AdTypeEnum, List<String>> FALLBACK_NETWORKS = Map.of(
            AdTypeEnum.BANNER, List.of("AdMob", "AdX", "Unity Ads"),
            AdTypeEnum.INTERSTITIAL, List.of("AdMob", "AdX", "IronSource"),
            AdTypeEnum.REWARDED, List.of("Unity Ads", "IronSource", "AdMob")
    );

    // Cache is only needed if filtering or Redis becomes a bottleneck, which is unlikely given Redis's performance.
    @CacheResult(cacheName = "country-priorities")
    public Map<AdTypeEnum, List<String>> getNetworkPrioritiesMap(String countryCode, PlatformEnum platform, String osVersion) {
        var adNetworkPriorities = redisService.get(countryCode);
        if (adNetworkPriorities == null) {
            adNetworkPriorities = FALLBACK_NETWORKS;
        }

        adNetworkPriorities = new HashMap<>(adNetworkPriorities);
        for (AdTypeEnum adTypeEnum : AdTypeEnum.values()) {
            var networks = adNetworkPriorities.getOrDefault(adTypeEnum, FALLBACK_NETWORKS.get(adTypeEnum));
            var filterNetworks = filterNetworks(countryCode, platform, osVersion, networks);
            adNetworkPriorities.put(adTypeEnum, filterNetworks);
        }

        return adNetworkPriorities;
    }

    public Map<String, Map<AdTypeEnum, List<String>>> getAllNetworkPriorities() {
        return redisService.getAll();
    }

    @CacheInvalidateAll(cacheName = "country-priorities")
    public void updateAdNetworkPriorities(Map<String, AdNetworkPriorities> prioritiesByCountry) {
        for (var entry : prioritiesByCountry.entrySet()) {
            redisService.set(entry.getKey(), entry.getValue().toMap());
        }
    }

    private List<String> filterNetworks(
            String countryCode,
            PlatformEnum platform,
            String osVersion,
            List<String> networks
    ) {
        List<String> filteredNetworks = new ArrayList<>(networks);

        if (platform == PlatformEnum.ANDROID && osVersion.startsWith("9.")) {
            filteredNetworks.remove("AdMob");
        }

        if ("CN".equals(countryCode)) {
            filteredNetworks.remove("Facebook");
        }

        if (!filteredNetworks.contains("AdMob")) {
            filteredNetworks.add("AdMob-OptOut");
        }

        return filteredNetworks;
    }

}
