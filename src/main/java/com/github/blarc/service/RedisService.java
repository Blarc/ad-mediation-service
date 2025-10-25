package com.github.blarc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.blarc.model.AdTypeEnum;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class RedisService {

    private static final String COUNTRY_KEY_PREFIX = "priorities:countries";
    private static final String COUNTRIES_SET_KEY = "priorities:all_countries";

    private final ValueCommands<String, Map<AdTypeEnum, List<String>>> valueCommands;
    private final SetCommands<String, String> setCommands;

    public RedisService(RedisDataSource ds) {
        valueCommands = ds.value(new TypeReference<>(){});
        setCommands = ds.set(String.class);
    }

    public void set(String countryCode, Map<AdTypeEnum, List<String>> countryPriorities) {
        String key = buildCountryKey(countryCode);
        valueCommands.set(key, countryPriorities);

        // Add country to the set of all countries
        setCommands.sadd(COUNTRIES_SET_KEY, countryCode);
    }

    public Map<AdTypeEnum, List<String>> get(String countryCode) {
        String key = buildCountryKey(countryCode);
        return valueCommands.get(key);
    }

    public Map<String, Map<AdTypeEnum, List<String>>> getAll() {
        Map<String, Map<AdTypeEnum, List<String>>> allPriorities = new HashMap<>();

        // Get all country codes from the set
        Set<String> countryCodes = setCommands.smembers(COUNTRIES_SET_KEY);

        // Fetch priorities for each country
        for (String countryCode : countryCodes) {
            Map<AdTypeEnum, List<String>> priorities = get(countryCode);
            if (priorities != null) {
                allPriorities.put(countryCode, priorities);
            }
        }
        return allPriorities;
    }

    public void delete(String countryCode) {
        String key = buildCountryKey(countryCode);
        valueCommands.getdel(key);
        setCommands.srem(COUNTRIES_SET_KEY, countryCode);
    }

    private String buildCountryKey(String countryCode) {
        return String.format("%s:%s", COUNTRY_KEY_PREFIX, countryCode);
    }
}
