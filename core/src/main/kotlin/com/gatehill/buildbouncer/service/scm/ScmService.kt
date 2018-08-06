package com.gatehill.buildbouncer.service.scm

interface ScmService {
    fun revertCommit(commit: String, branchName: String)
}
