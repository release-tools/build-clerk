import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.util.Color
import io.gatehill.buildclerk.dsl.config

config {
    buildFailed {
        if (commitHasEverSucceeded) {
            // commit has previously succeeded (on at least 1 branch)

            if (failuresForCommitOnBranch < 3) {
                rebuildBranch()
            } else {
                lockBranch()
            }

        } else {
            // commit has never succeeded on any branch
            revertCommit()
        }

        publishAnalysis(
                channelName = "general",
                color = Color.RED
        )
    }

    branchStartsPassing {
        postMessage(
                channelName = "general",
                message = "${report.name} branch `$branchName` is healthy again! ${report.build.fullUrl}",
                color = Color.GREEN
        )
    }

    repository {
        showText(
                title = "Show instructions",
                description = "hard reset branch $branchName to last passing commit",
                body = "Hard reset branch with command: ```git checkout $branchName && git reset $lastPassingCommitForBranch --hard```"
        )

        if (consecutiveFailuresOnBranch >= 2) {
            log("Branch `$branchName` has failed $consecutiveFailuresOnBranch time(s) consecutively")
            lockBranch()

            publishAnalysis(
                    channelName = "general",
                    color = Color.RED
            )
        }
    }

    pullRequestMerged {
        if (currentBranchStatus == BuildStatus.FAILED) {
            log("PR $prSummary was merged into failing branch `$branchName`")
            revertCommit()

            publishAnalysis(
                    channelName = "general",
                    color = Color.RED
            )

        } else {
            log("PR $prSummary was merged into branch `$branchName`")
            publishAnalysis(
                    channelName = "general",
                    color = Color.GREEN
            )
        }
    }

    buildPassed {
        postMessage(
                channelName = "general",
                message = "${report.name} build passed on branch `$branchName`: ${report.build.fullUrl}",
                color = Color.GREEN
        )
    }

    branchStartsFailing {
        postMessage(
                channelName = "general",
                message = "${report.name} branch `$branchName` is now failing: ${report.build.fullUrl}",
                color = Color.RED
        )
    }

    // send a branch summary every hour
    cron("0 0 * * * ?") {
        publishSummary(
            branchName = "branch",
            channelName = "general"
        )
    }
}
