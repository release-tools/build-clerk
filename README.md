SCM webhook receiver
====================

Receives PR merged event webhooks from BitBucket and verifies the status of the destination branch. 

## Build and run

Build it:

	./gradlew clean shadowjar

You can run it with:

	java -jar build/libs/scmwebhook.jar

## Configuration options

* REPO_NAME
* BRANCH_NAME

## Testing

Set configuration:

    export REPO_NAME="myrepo"
    export BRANCH_NAME="master"

Test branch status:

    curl -v -d '{ "buildNumber": 1, "outcome": "FAILED" }' -H 'Content-Type: application/json' http://localhost:9090/branches/master

Test PR merged event:

    curl -v -d @./examples/pr-merged.json -H 'Content-Type: application/json' http://localhost:9090/pull-requests/merged
