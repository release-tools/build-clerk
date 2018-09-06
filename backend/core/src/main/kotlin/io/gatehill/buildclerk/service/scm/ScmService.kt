package io.gatehill.buildclerk.service.scm

import io.gatehill.buildclerk.api.model.pr.RepoBranch
import io.gatehill.buildclerk.api.model.pr.SourceFile
import io.gatehill.buildclerk.model.scm.CommitUserInfo

interface ScmService {
    fun fetchUserInfoForCommit(commit: String): CommitUserInfo
    fun revertCommit(commit: String, branchName: String)
    fun lockBranch(branchName: String)
    fun listModifiedFiles(
        source: RepoBranch,
        destination: RepoBranch,
        sourceCommit: String,
        destinationCommit: String
    ): List<SourceFile>
}
