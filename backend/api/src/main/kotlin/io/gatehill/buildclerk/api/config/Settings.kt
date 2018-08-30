package io.gatehill.buildclerk.api.config

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Settings {
    object EventFilter {
        val repoNames: List<Regex> by lazy {
            System.getenv("FILTER_REPOS")?.split(",")?.map { it.toRegex() } ?: emptyList()
        }

        val branchNames: List<Regex> by lazy {
            System.getenv("FILTER_BRANCHES")?.split(",")?.map { it.toRegex() } ?: emptyList()
        }
    }

    class Repository(env: Map<String, String>? = null) : EnvironmentSettings(env) {
        val localDir: File by lazy {
            getenv("GIT_REPO_LOCAL_DIR")?.let { File(it) }
                ?: Files.createTempDirectory("git_repo").toFile()
        }

        val pushChanges: Boolean by lazy { getenv("GIT_REPO_PUSH_CHANGES")?.toBoolean() == true }

        /**
         * Examples:
         *
         * - ssh://user@example.com/repo.git
         * - https://github.com/user/repo.git
         */
        val remoteUrl: String by lazy {
            getenv("GIT_REPO_REMOTE_URL")
                ?: throw IllegalStateException("Missing Git remote URL")
        }

        val userName: String? by lazy { getenv("GIT_REPO_USERNAME") }

        val password: String? by lazy { getenv("GIT_REPO_PASSWORD") }

        /**
         * Overrides system strict host key checking.
         */
        val strictHostKeyChecking: Boolean? by lazy {
            getenv("GIT_REPO_STRICT_HOST_KEY_CHECKING")?.toBoolean()
        }
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
        val parseOnStartup: Boolean by lazy { System.getenv("RULES_PARSE_ON_STARTUP")?.toBoolean() != false }
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

    object Store {
        val implementation: String by lazy { System.getenv("STORE_IMPL") ?: "inmem" }
    }
}
