package com.github.blarc.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

import static com.github.blarc.model.AdTypeEnum.*;

public record AdNetworkPriorities(
        @Schema(description = "Ad networks for banner ads", examples = "[\"admob\", \"unity\"]")
        List<String> banner,
        @Schema(description = "Ad networks for interstitial ads", examples = "[\"admob\", \"applovin\"]")
        List<String> interstitial,
        @Schema(description = "Ad networks for rewarded ads", examples = "[\"unity\", \"admob\"]")
        List<String> rewarded
) {

    public static AdNetworkPriorities from(Map<AdTypeEnum, List<String>> map) {
        return new AdNetworkPriorities(
                map.get(BANNER),
                map.get(INTERSTITIAL),
                map.get(REWARDED)
        );
    }

    public Map<AdTypeEnum, List<String>> toMap() {
        return Map.of(
                BANNER, banner,
                INTERSTITIAL, interstitial,
                REWARDED, rewarded
        );
    }
}
