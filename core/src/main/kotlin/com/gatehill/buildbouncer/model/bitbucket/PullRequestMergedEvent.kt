package com.gatehill.buildbouncer.model.bitbucket

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequestMergedEvent(
        val actor: User,
        val repository: Repository,

        @JsonProperty("pullrequest")
        val pullRequest: PullRequest
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
        val username: String,

        @JsonProperty("display_name")
        val displayName: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequest(
        val id: Int,
        val title: String,
        val author: User,
        val source: RepoBranch,
        val destination: RepoBranch,

        @JsonProperty("merge_commit")
        val mergeCommit: Commit
)

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
