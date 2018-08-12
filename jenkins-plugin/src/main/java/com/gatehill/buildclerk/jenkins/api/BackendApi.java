package com.gatehill.buildclerk.jenkins.api;

import com.gatehill.buildclerk.api.model.BuildOutcome;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BackendApi {
    @POST("builds")
    Call<Void> notifyBuild(@Body BuildOutcome notification);
}
