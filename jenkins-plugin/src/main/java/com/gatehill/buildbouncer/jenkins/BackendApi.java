package com.gatehill.buildbouncer.jenkins;

import com.gatehill.buildbouncer.api.model.BuildOutcome;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BackendApi {
    @POST("builds")
    Call<Void> notifyBuild(@Body BuildOutcome notification);
}
