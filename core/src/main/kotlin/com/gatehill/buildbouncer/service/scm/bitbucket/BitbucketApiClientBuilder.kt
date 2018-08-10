package com.gatehill.buildbouncer.service.scm.bitbucket

import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.service.rest.ApiClientBuilder
import okhttp3.Credentials

/**
 * Builds Bitbucket REST client.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BitbucketApiClientBuilder : ApiClientBuilder<BitbucketApi> {
    override val baseUrl = "https://api.bitbucket.org/"

    override fun buildApiClient(headers: Map<String, String>): BitbucketApi {
        val allHeaders: Map<String, String> = headers.toMutableMap().apply {
            put("Authorization", Credentials.basic(Settings.Bitbucket.authUsername, Settings.Bitbucket.password))
        }

        return buildApiClient(BitbucketApi::class.java, allHeaders)
    }
}
