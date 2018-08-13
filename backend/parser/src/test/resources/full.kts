import com.gatehill.buildclerk.api.model.BuildStatus
import com.gatehill.buildclerk.dsl.Color
import com.gatehill.buildclerk.dsl.config

config {
    buildFailed {
        log("${outcome.name} build failed on branch: $branchName: ${outcome.build.fullUrl}")

        if (commitHasEverSucceeded) {
            log("Commit ${outcome.build.scm.commit} has previously succeeded (on at least 1 branch)")
            log("Commit has failed $failuresForCommitOnBranch time(s) on $branchName")

            if (failuresForCommitOnBranch < 3) {
                rebuildBranch()
            } else {
                lockBranch()
            }

        } else {
            log("Commit ${outcome.build.scm.commit} has never succeeded on any branch")
            revertCommit()
        }

        postAnalysisToChannel(
                channelName = "general",
                color = Color.RED
        )
    }

    branchStartsPassing {
        notifyChannel(
                channelName = "general",
                message = "${outcome.name} branch: $branchName is healthy again! ${outcome.build.fullUrl}",
                color = Color.GREEN
        )
    }

    repository {
        if (consecutiveFailuresOnBranch >= 2) {
            log("Branch $branchName has failed $consecutiveFailuresOnBranch times")
            lockBranch()

            postAnalysisToChannel(
                    channelName = "general",
                    color = Color.RED
            )
        }
    }

    pullRequestMerged {
        if (currentBranchStatus == BuildStatus.FAILED) {
            log("PR $prSummary was merged into failing branch $branchName by ${mergeEvent.actor.username}")
            revertCommit()

            postAnalysisToChannel(
                    channelName = "general",
                    color = Color.RED
            )

        } else {
            log("PR $prSummary was merged into branch $branchName by ${mergeEvent.actor.username}")
            postAnalysisToChannel(
                    channelName = "general",
                    color = Color.GREEN
            )
        }
    }

    buildPassed {
        notifyChannel(
                channelName = "general",
                message = "${outcome.name} build passed on branch: $branchName: ${outcome.build.fullUrl}",
                color = Color.GREEN
        )
    }

    branchStartsFailing {
        notifyChannel(
                channelName = "general",
                message = "${outcome.name} branch: $branchName is now failing: ${outcome.build.fullUrl}",
                color = Color.RED
        )
    }
}
