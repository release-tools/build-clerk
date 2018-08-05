package com.gatehill.buildbouncer.config

import java.io.File
import java.nio.file.Files

object Settings {
    val repoName: String by lazy { System.getenv("REPO_NAME") }
    val branchName: String by lazy { System.getenv("BRANCH_NAME") }

    object Thresholds {
        val maxFailuresForCommitOnBranch: Int by lazy { System.getenv("MAX_FAILURES_FOR_COMMIT")?.toInt() ?: 2 }
    }

    object Repository {
        val localDir: File by lazy {
            System.getenv("GIT_REPO_LOCAL_DIR")?.let { File(it) }
                    ?: Files.createTempDirectory("git_repo").toFile()
        }
        val pushChanges: Boolean by lazy { System.getenv("GIT_REPO_PUSH_CHANGES")?.toBoolean() == true }
    }
}
