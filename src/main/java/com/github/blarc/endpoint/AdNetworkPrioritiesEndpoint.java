package com.github.blarc.endpoint;

import com.github.blarc.model.AdNetworkPriorities;
import com.github.blarc.model.PlatformEnum;
import com.github.blarc.service.AdNetworkPrioritiesService;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.stream.Collectors;


@DenyAll
@ApplicationScoped
@Path("/ads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdNetworkPrioritiesEndpoint {

    @Inject
    AdNetworkPrioritiesService adNetworkPrioritiesService;

    @GET
    @Path("/{country_code}")
    @PermitAll
    public AdNetworkPriorities getAdNetworkPriorities(
            @PathParam("country_code") String countryCode,
            @QueryParam("platform") PlatformEnum platform,
            @QueryParam("os_version") String osVersion
    ) {
        if (!countryCode.matches("^[A-Z]{2}$")) {
            throw new BadRequestException("Invalid country code");
        }
        // it should return an ordered list of ad networks for each ad type
        var networkPrioritiesMap = adNetworkPrioritiesService.getNetworkPrioritiesMap(countryCode, platform, osVersion);
        return AdNetworkPriorities.from(networkPrioritiesMap);
    }

    @GET
    @RolesAllowed({"dashboard"})
    public Map<String, AdNetworkPriorities> getAdNetworkPriorities() {
        return adNetworkPrioritiesService.getAllNetworkPriorities().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> AdNetworkPriorities.from(entry.getValue())
                ));
    }

    @PUT
    @RolesAllowed({"processing"})
    public void updateAdNetworkPriorities(Map<String, AdNetworkPriorities> priorities) {
        priorities.forEach((countryCode, adNetworkPriorities) -> {
            if (!countryCode.matches("^[A-Z]{2}$")) {
                throw new BadRequestException("Invalid country code");
            }
        });
        adNetworkPrioritiesService.updateAdNetworkPriorities(priorities);
    }

}