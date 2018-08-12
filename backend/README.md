Backend server for Build Bouncer
================================

Run using Docker:

	 docker run --rm -it -p9090:9090 outofcoffee/build-bouncer

## Build and run JAR

If you want to build the JAR yourself, run this command from the root of the repository:

	./gradlew clean shadowjar

Run it:

	java -jar backend/server/build/libs/bouncer.jar

## Testing

Build notification:

    curl -v -d '{ "name": "example", "url": "job/example", "build": { "number": 1, "status": "FAILED", "full_url": "http://example.com/job/example/1", "scm": { "branch": "master", "commit": "c0ff33" } } }' -H 'Content-Type: application/json' http://localhost:9090/builds

Slack action trigger:

    curl -v -d @./examples/action-triggered.json -H 'Content-Type: application/json' http://localhost:9090/actions

PR merged event:

    curl -v -d @./examples/pr-merged.json -H 'Content-Type: application/json' http://localhost:9090/pull-requests/merged
