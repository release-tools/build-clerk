package com.gatehill.buildclerk.service.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

abstract class ApiClientBuilder<T> {
    abstract val baseUrl: String

    abstract fun buildApiClient(headers: Map<String, String> = emptyMap()): T

    /**
     * Builds an API client for the specified class using the baseUrl.
     */
    fun buildApiClient(clazz: Class<T>, headers: Map<String, String>): T {
        val correctedBaseUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl

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
            .baseUrl(correctedBaseUrl)
            .addConverterFactory(JacksonConverterFactory.create(jsonMapper))
            .build()
            .create(clazz)
    }

    companion object {
        private val jsonMapper by lazy { ObjectMapper().registerKotlinModule() }
    }
}
