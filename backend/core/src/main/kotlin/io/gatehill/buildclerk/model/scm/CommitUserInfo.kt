package io.gatehill.buildclerk.model.scm

data class CommitUserInfo(
    val author: ScmUser,
    val committer: ScmUser
)
