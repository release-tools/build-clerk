package com.gatehill.buildbouncer.service.rest

import com.gatehill.buildbouncer.util.jsonMapper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

interface ApiClientBuilder<T> {
    val baseUrl: String

    fun buildApiClient(headers: Map<String, String> = emptyMap()): T

    /**
     * Builds an API client for the specified class using the baseUrl.
     */
    fun buildApiClient(clazz: Class<T>, headers: Map<String, String>): T {
        if (!baseUrl.endsWith("/")) {
            throw RuntimeException("Base URL must end with a slash")
        }

        val clientBuilder = OkHttpClient.Builder()

        if (headers.isNotEmpty()) {
            clientBuilder.addInterceptor { interceptor ->
                val requestBuilder = interceptor.request().newBuilder()
                headers.forEach { requestBuilder.addHeader(it.key, it.value) }
                interceptor.proceed(requestBuilder.build())
            }
        }

        return Retrofit.Builder()
                .client(clientBuilder.build())
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(jsonMapper))
                .build()
                .create(clazz)
    }
}
