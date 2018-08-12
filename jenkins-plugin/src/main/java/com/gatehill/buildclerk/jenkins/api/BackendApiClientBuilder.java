package com.gatehill.buildclerk.jenkins.api;

import com.gatehill.buildclerk.service.rest.ApiClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * API client builder for backend server.
 */
public class BackendApiClientBuilder extends ApiClientBuilder<BackendApi> {
    private final String baseUrl;

    public BackendApiClientBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @NotNull
    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public BackendApi buildApiClient(@NotNull Map<String, String> headers) {
        return buildApiClient(BackendApi.class, headers);
    }
}
