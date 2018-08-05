Build Bouncer
=============

Respond to events in your build pipeline and keep you main branch stable.

## Examples

* Auto-reverts a commit after a failed build on your main branch, subject to certain thresholds and rules. 
* Receives PR merged event webhooks from BitBucket but your main branch is failing the build, so reverts the commit and notifies the team on Slack.

## Build and run

Build it:

	./gradlew clean shadowjar

You can run it with:

	java -jar build/libs/bouncer.jar

## Configuration options

* REPO_NAME
* BRANCH_NAME

## Testing

Set configuration:

    export REPO_NAME="myrepo"
    export BRANCH_NAME="master"

Test branch status:

    curl -v -d '{ "name": "example", "url": "job/example", "build": { "number": 1, "status": "FAILED", "full_url": "http://example.com/job/example/1", "scm": { "branch": "master", "commit": "c0ff33" } } }' -H 'Content-Type: application/json' http://localhost:9090/builds

Test PR merged event:

    curl -v -d @./examples/pr-merged.json -H 'Content-Type: application/json' http://localhost:9090/pull-requests/merged

Test incoming action notification:

    curl -v -d @./examples/action-triggered.json -H 'Content-Type: application/json' http://localhost:9090/actions
