package com.gatehill.buildbouncer.jenkins;

import com.gatehill.buildbouncer.service.rest.ApiClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * API client builder for backend server.
 */
public class BackendApiClientBuilder extends ApiClientBuilder<BackendApi> {
    private String baseUrl;

    @NotNull
    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(@NotNull String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public BackendApi buildApiClient(@NotNull Map<String, String> headers) {
        return buildApiClient(BackendApi.class, headers);
    }
}
