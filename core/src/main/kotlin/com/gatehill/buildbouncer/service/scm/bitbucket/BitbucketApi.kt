package com.gatehill.buildbouncer.service.scm.bitbucket

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Models the Bitbucket API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BitbucketApi {
    /**
     * Lists branch restriction rule for a repository.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/branch-restrictions#post
     */
    @GET("2.0/repositories/{username}/{repoSlug}/branch-restrictions")
    fun listBranchRestrictions(@Path("username") username: String,
                               @Path("repoSlug") repoSlug: String): Call<BitbucketList<BranchRestriction>>

    /**
     * Creates a new branch restriction rule for a repository.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/branch-restrictions#post
     */
    @POST("2.0/repositories/{username}/{repoSlug}/branch-restrictions")
    fun createBranchRestriction(@Path("username") username: String,
                                @Path("repoSlug") repoSlug: String,
                                @Body branchRestriction: BranchRestriction): Call<Void>

    /**
     * Updates an existing branch restriction rule for a repository.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/branch-restrictions#post
     */
    @PUT("2.0/repositories/{username}/{repoSlug}/branch-restrictions/{id}")
    fun updateBranchRestriction(@Path("username") username: String,
                                @Path("repoSlug") repoSlug: String,
                                @Path("id") id: Int,
                                @Body branchRestriction: BranchRestriction): Call<Void>
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitbucketList<T>(
        val values: List<T>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BranchRestriction(
        val kind: String,

        /**
         * Can be just the branch name.
         */
        val pattern: String,

        /**
         * Empty list signifies 'nobody'.
         */
        val users: List<Any> = emptyList(),

        /**
         * Empty list signifies 'nobody'.
         */
        val groups: List<BitbucketGroup> = emptyList(),

        val id: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitbucketGroup(
        val name: String,
        val slug: String
)
