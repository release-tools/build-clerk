package com.gatehill.buildclerk.config

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Settings {
    object EventFilter {
        val repoName: String? by lazy { System.getenv("REPO_NAME") }

        val branchName: String? by lazy { System.getenv("BRANCH_NAME") }
    }

    object Repository {
        val localDir: File by lazy {
            System.getenv("GIT_REPO_LOCAL_DIR")?.let { File(it) }
                    ?: Files.createTempDirectory("git_repo").toFile()
        }
        val pushChanges: Boolean by lazy { System.getenv("GIT_REPO_PUSH_CHANGES")?.toBoolean() == true }
    }

    object Jenkins {
        val baseUrl: String by lazy {
            System.getenv("JENKINS_BASE_URL")
                    ?: throw IllegalStateException("Missing Jenkins base URL")
        }

        val username: String? by lazy { System.getenv("JENKINS_USERNAME") }

        val password: String? by lazy { System.getenv("JENKINS_PASSWORD") }
    }

    object Slack {
        val userToken by lazy {
            System.getenv("SLACK_USER_TOKEN")
                    ?: throw IllegalStateException("Missing Slack user token")
        }
    }

    object Rules {
        val configFile: Path by lazy {
            System.getenv("RULES_FILE")?.let { Paths.get(it) }
                    ?: throw IllegalStateException("Missing rules file")
        }
    }

    object Server {
        val port: Int by lazy { System.getenv("SERVER_PORT")?.toInt() ?: 9090 }
    }

    object Bitbucket {
        val repoUsername: String by lazy {
            System.getenv("BITBUCKET_REPO_USERNAME")
                    ?: throw IllegalStateException("Missing Bitbucket repository username")
        }

        val repoSlug: String by lazy {
            System.getenv("BITBUCKET_REPO_SLUG")
                    ?: throw IllegalStateException("Missing Bitbucket repository slug")
        }

        val authUsername: String by lazy {
            System.getenv("BITBUCKET_AUTH_USERNAME")
                    ?: repoUsername
                    ?: throw IllegalStateException("Missing Bitbucket authentication username")
        }

        val password: String by lazy {
            System.getenv("BITBUCKET_PASSWORD")
                    ?: throw IllegalStateException("Missing Bitbucket password")
        }
    }
}
