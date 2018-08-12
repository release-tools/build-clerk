Build Clerk
===========

Respond to events in your build pipeline and keep your main branch stable.

## Examples

* Auto-revert a commit after a failed build on your main branch, subject to certain thresholds and rules. 
* Notify the team when a PR was merged whilst your main branch is failing on CI.

<img alt="Analysis recommends revert" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_revert.png" width="467">

## Configure your build

Clerk has a powerful DSL that lets you customise your rules:

```
if (failuresForCommitOnBranch <= 2) {
    rebuildBranch()
} else {
    revertCommit()
    lockBranch()
}

postAnalysisToChannel("general")
```

> This is just a taste of what you can do. See the examples in the `parser` module's tests for more.

## Run it

Run using Docker:

	 docker run --rm -it -p9090:9090 outofcoffee/build-clerk

See http://localhost:9090 to check it's running.

## Configuration options

* `RULES_FILE` (required) - path to rules file to respond to build lifecycle events

Slack configuration:

* `SLACK_USER_TOKEN` - a token with the `chat.write` permission

Jenkins configuration:

* `JENKINS_BASE_URL` - base URL for Jenkins server
* `JENKINS_USERNAME` - username for Jenkins
* `JENKINS_PASSWORD` - API key or password for Jenkins

Bitbucket configuration:

* `BITBUCKET_REPO_USERNAME`
* `BITBUCKET_REPO_SLUG`
* `BITBUCKET_AUTH_USERNAME`
* `BITBUCKET_PASSWORD`

Git repository configuration:

* `GIT_REPO_LOCAL_DIR` - path to a local repository clone
* `GIT_REPO_PUSH_CHANGES` - whether to push changes to the remote

Global filters:

* `BRANCH_NAME` - only process events for this SCM branch
* `REPO_NAME` - only process events from this repository (applies to PRs only)

Advanced configuration:

* `SERVER_PORT` - the HTTP port on which to listen
