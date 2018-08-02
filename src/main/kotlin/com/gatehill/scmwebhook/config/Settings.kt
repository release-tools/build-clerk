package com.gatehill.scmwebhook.config

object Settings {
    val repoName: String by lazy { System.getenv("REPO_NAME") }
    val branchName: String by lazy { System.getenv("BRANCH_NAME") }
}
