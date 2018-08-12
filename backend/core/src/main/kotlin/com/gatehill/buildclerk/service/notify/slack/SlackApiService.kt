package com.gatehill.buildclerk.service.notify.slack

import com.gatehill.buildclerk.config.Settings
import com.gatehill.buildclerk.util.jsonMapper
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.charset.Charset

/**
 * Interacts with the Slack API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackApiService {
    private val logger: Logger = LogManager.getLogger(SlackApiService::class.java)

    enum class HttpMethod {
        GET,
        POST
    }

    enum class BodyMode {
        FORM,
        JSON
    }

    inline fun <reified R> invokeSlackCommand(
            commandName: String,
            params: Map<String, *> = emptyMap<String, Any>(),
            method: HttpMethod = HttpMethod.POST,
            bodyMode: BodyMode = BodyMode.FORM
    ) = invokeSlackCommand(commandName, params, R::class.java, method, bodyMode)

    fun <R> invokeSlackCommand(
            commandName: String,
            params: Map<String, *>,
            responseClass: Class<R>,
            method: HttpMethod,
            bodyMode: BodyMode
    ): R {

        HttpClientBuilder.create().build().use { httpClient ->
            // invoke command
            try {
                val uriBuilder = URIBuilder().apply {
                    scheme = "https"
                    host = "slack.com"
                    path = "/api/$commandName"
                }

                val request = when (method) {
                    HttpMethod.GET -> {
                        uriBuilder.addParameters(buildNameValuePairs(params))
                        HttpGet(uriBuilder.build())
                    }
                    HttpMethod.POST -> {
                        HttpPost(uriBuilder.build()).apply {
                            when (bodyMode) {
                                BodyMode.FORM -> this.entity =
                                        UrlEncodedFormEntity(buildNameValuePairs(params), "UTF-8")

                                BodyMode.JSON -> {
                                    this.addHeader("Authorization", "Bearer ${Settings.Slack.userToken}")
                                    this.entity = StringEntity(generateJsonBody(params), ContentType.APPLICATION_JSON)
                                }
                            }
                        }
                    }
                }

                httpClient.execute(request).let { response ->
                    response.entity.content.use {
                        val jsonResponse = String(it.readBytes(), Charset.forName("UTF-8"))
                        logger.debug("Slack API: $commandName returned HTTP status: ${response.statusLine.statusCode}")
                        logger.trace("Slack API: $commandName returned: $jsonResponse")

                        return parseResponse(jsonResponse, responseClass)
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error calling Slack API: $commandName", e)
            }
        }
    }

    private fun <R> parseResponse(
            jsonResponse: String,
            responseClass: Class<R>
    ): R {
        val parsedResponse = jsonMapper.readValue(jsonResponse, responseClass)
        when (parsedResponse) {
            is Map<*, *> -> if (true != parsedResponse["ok"]) {
                throw RuntimeException("Expected response 'ok' field to be true: $jsonResponse")
            }
            else -> logger.warn("Unable to check response for type: $responseClass - payload: $jsonResponse")
        }
        return parsedResponse
    }

    private fun buildNameValuePairs(params: Map<String, Any?>): List<NameValuePair> {
        val payload = mutableListOf<NameValuePair>()
        for ((key, value) in params) {
            if (key != "token") {
                payload.add(BasicNameValuePair(key, value?.toString()))
            }
        }
        payload += BasicNameValuePair("token", Settings.Slack.userToken)
        return payload
    }

    private fun generateJsonBody(params: Map<String, *>) = jsonMapper.writeValueAsString(params)
}
