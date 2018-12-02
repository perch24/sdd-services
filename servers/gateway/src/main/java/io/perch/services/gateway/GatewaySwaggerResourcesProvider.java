package io.perch.services.gateway;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Primary
public class GatewaySwaggerResourcesProvider implements SwaggerResourcesProvider {
    private final RouteLocator routeLocator;
    private final WebClient.Builder webClient;
    private final GatewaySwaggerConfig gatewaySwaggerConfig;
    private final Cache<String, SwaggerProxyResource> resourceCache;
    private final Logger log = LoggerFactory.getLogger(GatewaySwaggerResourcesProvider.class);

    public GatewaySwaggerResourcesProvider(GatewaySwaggerConfig gatewaySwaggerConfig, RouteLocator routeLocator, WebClient.Builder webClient) {
        this.gatewaySwaggerConfig = gatewaySwaggerConfig;
        this.routeLocator = routeLocator;
        this.webClient = webClient;
        this.resourceCache = Caffeine.newBuilder().expireAfterWrite(
                Duration.ofMillis(Math.round(gatewaySwaggerConfig.getRefreshRate() * 1.25))).build();
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        //Add the default swagger resource that correspond to the gateway's own swagger doc
        resources.add(swaggerResource(null,"GATEWAY : default", "/v2/api-docs", "2.0"));

        var proxiedResources = new ArrayList<>(resourceCache.asMap().values());
        Collections.sort(proxiedResources);
        resources.addAll(proxiedResources);

        return resources;
    }

    @Scheduled(fixedRateString = "${gateway.swagger.refreshRate:60000}")
    public void updateResources() {
        routeLocator.getRoutes()
                .flatMap(route -> {
                    if (isIgnoredRoute(route)) {
                        return Flux.empty();
                    }
                    final var routeUriBuilder = UriComponentsBuilder.fromUri(route.getUri());
                    var routeUri = routeUriBuilder.pathSegment("swagger-resources").build();
                    return Flux.zip(Mono.just(route),
                            webClient.build().get().uri(routeUri.toUri()).exchange()
                                    .onErrorResume(e -> {
                                        var invalidated = invalidateSwaggerResourceCacheForRoute(route);
                                        if (invalidated > 0) {
                                            log.debug("Ignoring route due to http error, route = {}, message = {}, invalidated = {}",
                                                    route.getUri(), e.getMessage(), invalidated);
                                        }
                                        return Mono.empty();
                                    })
                                    .flatMapMany(clientResponse -> {
                                        if (clientResponse.statusCode().isError()) {
                                            var invalidated = invalidateSwaggerResourceCacheForRoute(route);
                                            if (invalidated > 0) {
                                                log.debug("Removing route due to http error, route = {}, http status = {}, invalidated = {}",
                                                        route.getUri(), clientResponse.rawStatusCode(), invalidated);
                                            }
                                            return Flux.empty();
                                        } else {
                                            return clientResponse.bodyToFlux(SwaggerResource.class);
                                        }
                                    }));
                })
                .flatMap(tuple -> {
                    var route = tuple.getT1();
                    var swaggerResource = tuple.getT2();
                    var swaggerResourceUrl = UriComponentsBuilder.fromUriString(swaggerResource.getUrl()).build();

                    final var proxyUriBuilder = UriComponentsBuilder.newInstance();
                    proxyUriBuilder.path("/" + route.getUri().getHost().toLowerCase());
                    if (!StringUtils.isEmpty(route.getUri().getPath())) {
                        proxyUriBuilder.pathSegment(route.getUri().getPath());
                    }
                    proxyUriBuilder.pathSegment(swaggerResourceUrl.getPath());
                    if (!CollectionUtils.isEmpty(swaggerResourceUrl.getQueryParams())) {
                        proxyUriBuilder.queryParams(swaggerResourceUrl.getQueryParams());
                    }

                    var name = route.getUri().getHost() + " : " + swaggerResource.getName();
                    return Mono.just(swaggerResource(route, name, proxyUriBuilder.build().toString(), swaggerResource.getSwaggerVersion()));
                })
                .subscribe(swaggerResource -> cacheSwaggerResource(swaggerResource));
    }

    private void cacheSwaggerResource(SwaggerProxyResource swaggerResource) {
        resourceCache.put(swaggerResource.getName(), swaggerResource);
    }

    private int invalidateSwaggerResourceCacheForRoute(Route route) {
        return resourceCache.asMap().entrySet().stream().filter(e -> route.equals(e.getValue().getRoute())).mapToInt(e -> {
            log.info("Invalidating swagger resource, name = {}, route = {}", e.getValue().getName(), route.getUri());
            resourceCache.invalidate(e.getKey());
            return 1;
        }).sum();
    }

    private boolean isIgnoredRoute(Route route) {
        for (var ignored : gatewaySwaggerConfig.getIgnoredRoutes()) {
            if (route.getUri().toString().toLowerCase().endsWith(ignored)) {
                return true;
            }
        }
        return false;
    }

    private SwaggerProxyResource swaggerResource(Route route, String name, String location, String version) {
        SwaggerProxyResource swaggerResource = new SwaggerProxyResource(route);
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }

    static class SwaggerProxyResource extends SwaggerResource {
        private final Route route;

        public SwaggerProxyResource(Route route) {
            this.route = route;
        }

        @JsonIgnore
        public Route getRoute() {
            return route;
        }
    }
    @Configuration
    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "gateway.swagger")
    static class GatewaySwaggerConfig {
        private List<String> ignoredRoutes = new ArrayList<>();
        private long refreshRate = 0l;

        public List<String> getIgnoredRoutes() {
            return ignoredRoutes;
        }

        public void setIgnoredRoutes(List<String> ignoredRoutes) {
            this.ignoredRoutes = ignoredRoutes;
        }

        public long getRefreshRate() {
            return refreshRate;
        }

        public void setRefreshRate(long refreshRate) {
            this.refreshRate = refreshRate;
        }
    }
}
