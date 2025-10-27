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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;
import java.util.stream.Collectors;


@DenyAll
@ApplicationScoped
@Path("/ads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ad Network Priorities", description = "Manage ad network priorities for different countries and platforms")
public class AdNetworkPrioritiesEndpoint {

    @Inject
    AdNetworkPrioritiesService adNetworkPrioritiesService;

    @GET
    @Path("/{country_code}")
    @PermitAll
    @Operation(
            summary = "Get ad network priorities for a country",
            description = "Returns prioritized list of ad networks for each ad type (banner, interstitial, rewarded video) " +
                    "filtered by platform and OS version. Used by mobile applications to determine which ad networks to use."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Successfully retrieved ad network priorities",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AdNetworkPriorities.class)
                    ),
                    headers = {
                            @org.eclipse.microprofile.openapi.annotations.headers.Header(
                                    name = "Cache-Control",
                                    description = "Cache control directive",
                                    schema = @Schema(type = SchemaType.STRING)
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid country code format (must be 2 uppercase letters)"
            )
    })
    public AdNetworkPriorities getAdNetworkPriorities(
            @Parameter(
                    description = "Two-letter country code (ISO 3166-1 alpha-2)",
                    required = true,
                    example = "US",
                    schema = @Schema(pattern = "^[A-Z]{2}$")
            )
            @PathParam("country_code") String countryCode,

            @Parameter(
                    description = "Mobile platform",
                    example = "ANDROID"
            )
            @QueryParam("platform") PlatformEnum platform,

            @Parameter(
                    description = "Operating system version",
                    example = "9.3.1"
            )
            @QueryParam("os_version") String osVersion
    ) {
        if (!countryCode.matches("^[A-Z]{2}$")) {
            throw new BadRequestException("Invalid country code");
        }

        var networkPrioritiesMap = adNetworkPrioritiesService.getNetworkPrioritiesMap(countryCode, platform, osVersion);
        return AdNetworkPriorities.from(networkPrioritiesMap);
    }

    @GET
    @RolesAllowed({"dashboard"})
    @Operation(
            summary = "Get all ad network priorities",
            description = "Returns ad network priorities for all countries. Used by internal dashboard for monitoring and analytics. " +
                    "Requires 'dashboard' role for access."
    )
    @SecurityRequirement(name = "basicAuth")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all priorities",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = Map.class,
                                    description = "Map of country code to ad network priorities"
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Insufficient permissions (requires 'dashboard' role)"
            )
    })
    public Map<String, AdNetworkPriorities> getAllAdNetworkPriorities() {
        return adNetworkPrioritiesService.getAllNetworkPriorities().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> AdNetworkPriorities.from(entry.getValue())
                ));
    }

    @PUT
    @RolesAllowed({"processing"})
    @Operation(
            summary = "Update ad network priorities",
            description = "Batch update ad network priorities from data pipeline processing. " +
                    "Replaces existing priorities for all specified countries. " +
                    "Requires 'processing' role for access."
    )
    @SecurityRequirement(name = "basicAuth")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Priorities successfully updated"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid request body or country code format"
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Insufficient permissions (requires 'processing' role)"
            )
    })
    public void updateAdNetworkPriorities(
            @RequestBody(
                    description = "Map of country codes to ad network priorities. Each country must have priorities for all ad types.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.OBJECT,
                                    implementation = Map.class,
                                    examples = """
                            {
                              "US": {
                                "banner": ["AdMob", "AdX", "Unity Ads"],
                                "interstitial": ["AdX", "AdMob", "IronSource"],
                                "rewarded_video": ["Unity Ads", "IronSource", "AdMob"]
                              },
                              "UK": {
                                "banner": ["AdX", "AdMob", "Facebook"],
                                "interstitial": ["AdMob", "AdX", "Unity Ads"],
                                "rewarded_video": ["Unity Ads", "AdMob", "IronSource"]
                              }
                            }
                            """
                            )
                    )
            )
            Map<String, AdNetworkPriorities> priorities
    ) {
        priorities.forEach((countryCode, adNetworkPriorities) -> {
            if (!countryCode.matches("^[A-Z]{2}$")) {
                throw new BadRequestException("Invalid country code");
            }
        });
        adNetworkPrioritiesService.updateAdNetworkPriorities(priorities);
    }
}