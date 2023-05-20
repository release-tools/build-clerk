package io.gatehill.buildclerk.api.model.pr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

enum class PullRequestEventType {
    CREATED,
    UPDATED;

    override fun toString() = super.toString().lowercase()

    companion object {

        fun parse(eventKey: String): PullRequestEventType = when (eventKey) {
            "pullrequest:created" -> CREATED
            "pullrequest:updated" -> UPDATED
            else -> throw IllegalStateException("Unsupported event key $eventKey")
        }
    }
}

interface PullRequestEvent {
    val actor: User
    val repository: Repository
    val pullRequest: PullRequest
}

/**
 * A PR has been created or updated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequestModifiedEvent(
    override val actor: User,
    override val repository: Repository,

    @JsonProperty("pullrequest")
    override val pullRequest: PullRequest
) : PullRequestEvent

/**
 * A PR has been merged.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequestMergedEvent(
    override val actor: User,
    override val repository: Repository,

    @JsonProperty("pullrequest")
    override val pullRequest: MergedPullRequest
) : PullRequestEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val username: String,

    @JsonProperty("display_name")
    val displayName: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
open class PullRequest(
    val id: Int,
    val title: String,
    val author: User,
    val source: RepoBranch,
    val destination: RepoBranch
) {
    override fun toString(): String {
        return "PullRequest(id=$id, title='$title', author=$author, source=$source, destination=$destination)"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class MergedPullRequest(
    id: Int,
    title: String,
    author: User,
    source: RepoBranch,
    destination: RepoBranch,

    @JsonProperty("merge_commit")
    val mergeCommit: Commit

) : PullRequest(
    id = id,
    title = title,
    author = author,
    source = source,
    destination = destination
) {
    override fun toString(): String {
        return "MergedPullRequest(${super.toString()}, mergeCommit=$mergeCommit)"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RepoBranch(
    val branch: Branch,
    val commit: Commit,
    val repository: Repository
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Branch(
    val name: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Commit(
    val hash: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Repository(
    val name: String
)
