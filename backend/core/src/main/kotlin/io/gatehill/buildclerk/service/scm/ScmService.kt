package io.gatehill.buildclerk.service.scm

import io.gatehill.buildclerk.model.scm.CommitUserInfo

interface ScmService {
    fun fetchUserInfoForCommit(commit: String): CommitUserInfo
    fun revertCommit(commit: String, branchName: String)
    fun lockBranch(branchName: String)
}
