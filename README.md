Build Bouncer
=============

Respond to events in your build pipeline and keep your main branch stable.

## Examples

* Auto-revert a commit after a failed build on your main branch, subject to certain thresholds and rules. 
* Notify the team when a PR was merged whilst your main branch is failing on CI.

<img alt="Analysis recommends revert" src="https://github.com/outofcoffee/build-bouncer/raw/master/docs/img/build_analysis_revert.png" width="467">

## Configure your build

Bouncer has a powerful DSL that lets you customise your rules:

```
if (failuresForCommitOnBranch <= 2) {
    rebuildBranch()
} else {
    revertCommit()
    lockBranch()
}

postAnalysisToChannel("general")
```

> This is just a taste of what you can do. See the examples in the `parser` module for more.

## Run it

Run using Docker:

	 docker run --rm -it -p9090:9090 outofcoffee/build-bouncer

See http://localhost:9090 to check it's running.

## Configuration options

* RULES_FILE - Path to rules file to respond to build lifecycle events

Git repository configuration:

* GIT_REPO_LOCAL_DIR - Path to a local repository clone
* GIT_REPO_PUSH_CHANGES - Whether to push changes to the remote

Jenkins configuration:

* JENKINS_BASE_URL
* JENKINS_USERNAME
* JENKINS_PASSWORD

Slack configuration:

* SLACK_USER_TOKEN - A token with the `chat.write` permission

Bitbucket configuration:

* BITBUCKET_REPO_USERNAME
* BITBUCKET_REPO_SLUG
* BITBUCKET_AUTH_USERNAME
* BITBUCKET_PASSWORD

Configuration for pull request events:

* REPO_NAME
* BRANCH_NAME

Advanced configuration:

* SERVER_PORT
