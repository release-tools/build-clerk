import com.gatehill.buildbouncer.dsl.config

config {
    buildFailed {
        log("Job ${outcome.name} build failed: ${outcome.build.fullUrl}")

        if (hasEverSucceeded()) {
            log("Commit ${outcome.build.scm.commit} has previously succeeded (on at least 1 branch)")

            val failuresForCommitOnBranch = countFailuresForCommitOnBranch()
            log("Commit has failed $failuresForCommitOnBranch time(s) on ${outcome.build.scm.branch}")

            if (failuresForCommitOnBranch < 3) {
                rebuildBranch()
            } else {
                lockBranch()
            }

        } else {
            log("Commit ${outcome.build.scm.commit} has never succeeded on any branch")
            revertCommit()
        }

        notifyChannel(
            channelName = "general",
            analysis = analysis,
            color = "#ff0000"
        )
    }

    buildPassed {
        /* Nothing */
    }

    branchStartsPassing {
        notifyChannel(
            channelName = "general",
            message = "Job ${outcome.name} is healthy again! ${outcome.build.fullUrl}",
            color = "#00ff00"
        )
    }

    branchStartsFailing {
        /* Nothing */
    }

    repository {
        /* Nothing */
    }
}
