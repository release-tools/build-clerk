import com.gatehill.buildbouncer.dsl.config

config {
    build {
        log("Build failed: ${outcome.build.fullUrl}")

        if (hasEverSucceeded()) {
            log("Commit ${outcome.build.scm.commit} has previously succeeded (on at least 1 branch)")

            val failuresForCommitOnBranch = countFailuresForCommitOnBranch()
            log("Commit has failed $failuresForCommitOnBranch time on ${outcome.build.scm.branch}")

            if (failuresForCommitOnBranch < 2) {
                log("Rebuilding ${outcome.build.number} on ${outcome.build.scm.branch}")
                rebuild()
            } else {
                revertCommit()
            }

        } else {
            log("Commit ${outcome.build.scm.commit} has never succeeded on any branch")
            revertCommit()
        }
    }

    repository {
        /* Nothing */
    }
}
