package com.gatehill.buildclerk.service.scm

interface ScmService {
    fun revertCommit(commit: String, branchName: String)
    fun lockBranch(branchName: String)
}
