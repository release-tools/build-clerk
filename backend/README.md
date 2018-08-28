Backend server for Build Clerk
==============================

Run using Docker:

    docker run --rm -it -p9090:9090 outofcoffee/build-clerk

## Build and run JAR

If you want to build the JAR yourself, run this command from the root of the repository:

    ./gradlew clean shadowjar

Run it:

    java -jar backend/server/build/libs/clerk.jar

## Testing

Failed build report:

    curl -v -d @./examples/build-failed.json -H 'Content-Type: application/json' http://localhost:9090/builds

Slack action trigger:

    curl -v -d @./examples/action-triggered.json -H 'Content-Type: application/json' http://localhost:9090/actions

PR merged event:

    curl -v -d @./examples/pr-merged.json -H 'Content-Type: application/json' -H 'X-Event-Key: pullrequest:fulfilled' http://localhost:9090/pull-requests/merged
