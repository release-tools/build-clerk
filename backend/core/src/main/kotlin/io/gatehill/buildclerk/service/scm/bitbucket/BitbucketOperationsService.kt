package io.gatehill.buildclerk.service.scm.bitbucket

import io.gatehill.buildclerk.api.config.Settings
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

/**
 * Common Bitbucket operations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BitbucketOperationsService @Inject constructor(
    private val apiClientBuilder: BitbucketApiClientBuilder
) {
    private val logger: Logger = LogManager.getLogger(BitbucketOperationsService::class.java)

    fun listRestrictions(branchName: String, apiClient: BitbucketApi): List<BranchRestriction> {
        val call = apiClient.listBranchRestrictions(
            username = Settings.Bitbucket.repoUsername,
            repoSlug = Settings.Bitbucket.repoSlug
        )
        val response = call.execute()

        return if (response.isSuccessful) {
            response.body().values
        } else {
            throw RuntimeException("Unsuccessfully listed branch restriction [branch name: $branchName, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
        }
    }

    fun ensureRestriction(
        apiClient: BitbucketApi,
        branchName: String,
        restrictions: List<BranchRestriction>,
        kind: String
    ) {
        val existing = restrictions.firstOrNull { restriction ->
            restriction.kind == kind && restriction.pattern == branchName
        }

        existing?.let { updateRestriction(apiClient, branchName, kind, existing.id!!) }
            ?: addRestriction(apiClient, branchName, kind)
    }

    private fun addRestriction(
        apiClient: BitbucketApi,
        branchName: String,
        kind: String
    ) {
        logger.debug("Adding branch restriction: [branch name: $branchName, kind: $kind]")

        val call: Call<Void> = apiClient.createBranchRestriction(
            username = Settings.Bitbucket.repoUsername,
            repoSlug = Settings.Bitbucket.repoSlug,
            branchRestriction = BranchRestriction(
                kind = kind,
                pattern = branchName
            )
        )

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                handleRestrictionResponse(branchName, kind, call, response)
            } else {
                throw RuntimeException("Unsuccessfully added branch restriction [branch name: $branchName, kind: $kind, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }

        } catch (e: Exception) {
            throw RuntimeException("Error adding branch restriction: [branch name: $branchName, kind: $kind]", e)
        }
    }

    private fun updateRestriction(
        apiClient: BitbucketApi,
        branchName: String,
        kind: String,
        branchRestrictionId: Int
    ) {
        logger.debug("Updating branch restriction: [branch name: $branchName, kind: $kind, id: $branchRestrictionId]")

        val call: Call<Void> = apiClient.updateBranchRestriction(
            username = Settings.Bitbucket.repoUsername,
            repoSlug = Settings.Bitbucket.repoSlug,
            id = branchRestrictionId,
            branchRestriction = BranchRestriction(
                kind = kind,
                pattern = branchName
            )
        )

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                handleRestrictionResponse(branchName, kind, call, response)
            } else {
                throw RuntimeException("Unsuccessfully updated branch restriction [branch name: $branchName, kind: $kind, id: $branchRestrictionId, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }

        } catch (e: Exception) {
            throw RuntimeException(
                "Error updating branch restriction: [branch name: $branchName, kind: $kind, id: $branchRestrictionId]",
                e
            )
        }
    }

    /**
     * Process the response to adding a restriction.
     */
    private fun handleRestrictionResponse(
        branchName: String,
        kind: String,
        call: Call<Void>,
        response: Response<Void>
    ) {
        when (response.code()) {
            200, 201 -> {
                logger.info("Set branch restriction: [branch name: $branchName, kind: $kind]")
            }
            else -> {
                throw RuntimeException("Unsuccessfully set branch restriction [branch name: $branchName, kind: $kind, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }
        }
    }

    fun listComments(pullRequestId: Int): List<PullRequestComment> {
        logger.debug("Checking for comments on PR #$pullRequestId")

        val apiClient = apiClientBuilder.buildApiClient()

        val call: Call<BitbucketList<PullRequestComment>> = apiClient.listPullRequestComments(
            username = Settings.Bitbucket.repoUsername,
            repoSlug = Settings.Bitbucket.repoSlug,
            pullRequestId = pullRequestId
        )

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                return response.body().values
            } else {
                throw RuntimeException("Unsuccessfully listed PR comments [PR: $pullRequestId, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }

        } catch (e: Exception) {
            throw RuntimeException("Error listing PR comments [PR: $pullRequestId]", e)
        }
    }

    fun createComment(pullRequestId: Int, comment: String) {
        logger.debug("Adding comment to PR #$pullRequestId: $comment")

        val apiClient = apiClientBuilder.buildApiClient()

        val call: Call<PullRequestComment> = apiClient.createPullRequestComment(
            username = Settings.Bitbucket.repoUsername,
            repoSlug = Settings.Bitbucket.repoSlug,
            pullRequestId = pullRequestId,
            comment = PullRequestComment(
                content = CommentContent(
                    raw = comment
                )
            )
        )

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val createdComment = response.body()
                logger.debug("Added PR comment [PR: $pullRequestId, comment: $comment] with ID: ${createdComment.id}")
            } else {
                throw RuntimeException("Unsuccessfully added PR comment [PR: $pullRequestId, comment: $comment, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }

        } catch (e: Exception) {
            throw RuntimeException("Error adding PR comment [PR: $pullRequestId, comment: $comment]", e)
        }
    }

    fun fetchDiffstat(pullRequestId: Int): List<Diffstat> {
        val diffstat = mutableListOf<Diffstat>()

        val apiClient = apiClientBuilder.buildApiClient()
        var diffstatUrl: String

        // initial URL
        diffstatUrl = fetchDiffstatUrl(apiClient, pullRequestId)

        var pages = 0
        while (pages++ < MAX_PAGES) {
            try {
                val diffstatResponse = fetchDiffstat(apiClient, diffstatUrl)
                diffstat += diffstatResponse.values
                diffstatResponse.next?.let { next -> diffstatUrl = next } ?: break
            } catch (e: Exception) {
                throw RuntimeException("Error fetching diffstat for PR #$pullRequestId", e)
            }
        }

        return diffstat
    }

    /**
     * @return the diffstat URL for the given PR
     */
    private fun fetchDiffstatUrl(apiClient: BitbucketApi, pullRequestId: Int): String {
        logger.debug("Fetching diffstat URL for PR #$pullRequestId")

        val call: Call<Void> = apiClient.fetchDiffstatUrl(
            username = Settings.Bitbucket.repoUsername,
            repoSlug = Settings.Bitbucket.repoSlug,
            pullRequestId = pullRequestId
        )

        try {
            val response = call.execute()
            if (response.code() in 301..302) {
                response.headers()["Location"]?.let { diffstatUrl ->
                    logger.debug("Fetched diffstat URL for PR #$pullRequestId: $diffstatUrl")
                    return diffstatUrl

                } ?: throw IllegalStateException(
                    "Missing Location header when fetching diffstat URL for PR #$pullRequestId [request URL: ${call.request().url()}, response code: ${response.code()}]"
                )

            } else {
                throw RuntimeException("Unsuccessfully fetched diffstat URL for PR #$pullRequestId [request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }

        } catch (e: Exception) {
            throw RuntimeException("Error fetching diffstat URL for PR #$pullRequestId", e)
        }
    }

    /**
     * @return the diffstat from the given URL
     */
    private fun fetchDiffstat(apiClient: BitbucketApi, diffstatUrl: String): BitbucketList<Diffstat> {
        logger.debug("Fetching diffstat for PR from URL: $diffstatUrl")

        val call = apiClient.fetchDiffstat(diffstatUrl)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                return response.body()
            } else {
                throw RuntimeException("Unsuccessfully fetched diffstat for PR [request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}")
            }

        } catch (e: Exception) {
            throw RuntimeException("Error fetching diffstat for PR", e)
        }
    }

    companion object {
        private const val MAX_PAGES = 30
    }
}
