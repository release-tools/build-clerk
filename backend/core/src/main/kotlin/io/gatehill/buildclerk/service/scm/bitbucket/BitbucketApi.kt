package io.gatehill.buildclerk.service.scm.bitbucket

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

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
    fun listBranchRestrictions(
        @Path("username") username: String,
        @Path("repoSlug") repoSlug: String
    ): Call<BitbucketList<BranchRestriction>>

    /**
     * Creates a new branch restriction rule for a repository.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/branch-restrictions#post
     */
    @POST("2.0/repositories/{username}/{repoSlug}/branch-restrictions")
    fun createBranchRestriction(
        @Path("username") username: String,
        @Path("repoSlug") repoSlug: String,
        @Body branchRestriction: BranchRestriction
    ): Call<Void>

    /**
     * Updates an existing branch restriction rule for a repository.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/branch-restrictions#post
     */
    @PUT("2.0/repositories/{username}/{repoSlug}/branch-restrictions/{id}")
    fun updateBranchRestriction(
        @Path("username") username: String,
        @Path("repoSlug") repoSlug: String,
        @Path("id") id: Int,
        @Body branchRestriction: BranchRestriction
    ): Call<Void>

    /**
     * List the comments on a PR.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests/%7Bpull_request_id%7D/comments
     */
    @GET("2.0/repositories/{username}/{repoSlug}/pullrequests/{pullRequestId}/comments")
    fun listPullRequestComments(
        @Path("username") username: String,
        @Path("repoSlug") repoSlug: String,
        @Path("pullRequestId") pullRequestId: Int
    ): Call<BitbucketList<PullRequestComment>>

    /**
     * Create a comment on a PR.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests/%7Bpull_request_id%7D/comments
     */
    @POST("2.0/repositories/{username}/{repoSlug}/pullrequests/{pullRequestId}/comments")
    fun createPullRequestComment(
        @Path("username") username: String,
        @Path("repoSlug") repoSlug: String,
        @Path("pullRequestId") pullRequestId: Int,
        @Body comment: PullRequestComment
    ): Call<PullRequestComment>

    /**
     * Fetch the location (in the form of an HTTP 302 Redirect) of the PR diffstat.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/pullrequests/%7Bpull_request_id%7D/diffstat
     */
    @GET("2.0/repositories/{username}/{repoSlug}/pullrequests/{pullRequestId}/diffstat")
    fun fetchDiffstatUrl(
        @Path("username") username: String,
        @Path("repoSlug") repoSlug: String,
        @Path("pullRequestId") pullRequestId: Int
    ): Call<Void>

    /**
     * Fetch a diffstat.
     *
     * See https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D/diffstat/%7Bspec%7D
     */
    @GET
    fun fetchDiffstat(
        @Url url: String
    ): Call<BitbucketList<Diffstat>>
}

/**
 * See https://developer.atlassian.com/bitbucket/api/2/reference/meta/pagination
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BitbucketList<T>(
    val values: List<T>,

    /**
     * Current number of objects on the existing page.
     * The default value is 10 with 100 being the maximum allowed value. Individual APIs may enforce different values.
     */
    @JsonProperty("pagelen")
    val pageLength: Int,

    /**
     * Page number of the current results.
     * This is an optional element that is not provided in all responses.
     */
    val page: Int?,

    /**
     * Total number of objects in the response.
     * This is an optional element that is not provided in all responses, as it can be expensive to compute.
     */
    val size: Int?,

    /**
     * Link to the next page if it exists. The last page of a collection does not have this value.
     * Use this link to navigate the result set and refrain from constructing your own URLs.
     */
    val next: String?
)

/**
 * Don't serialise null IDs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequestComment(
    val id: String? = null,
    val content: CommentContent
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentContent(
    val raw: String
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Diffstat(
    val status: String,

    @JsonProperty("lines_added")
    val linesAdded: Int,

    @JsonProperty("lines_removed")
    val linesRemoved: Int,

    val old: DiffstatFile?,
    val new: DiffstatFile?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DiffstatFile(
    val path: String?,
    val type: String?
)
