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

## Build and run

Build it:

	./gradlew clean shadowjar

Run it:

	java -jar server/build/libs/bouncer.jar

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

## Testing

Build notification:

    curl -v -d '{ "name": "example", "url": "job/example", "build": { "number": 1, "status": "FAILED", "full_url": "http://example.com/job/example/1", "scm": { "branch": "master", "commit": "c0ff33" } } }' -H 'Content-Type: application/json' http://localhost:9090/builds

Slack action trigger:

    curl -v -d @./examples/action-triggered.json -H 'Content-Type: application/json' http://localhost:9090/actions

PR merged event:

    curl -v -d @./examples/pr-merged.json -H 'Content-Type: application/json' http://localhost:9090/pull-requests/merged
