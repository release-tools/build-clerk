Build Clerk Jenkins plugin
============================

Reports build status to a Build Clerk server.

This plugin supports use as both a classic Jenkins post-build step, as well as Jenkins pipeline step.

## Installation

Build the plugin file `build-clerk.hpi` by running this command from the root of the repository:

    ./gradlew jpi

The HPI file built to:

    jenkins-plugin/build/libs/build-clerk.hpi

Upload this in the 'Advanced' tab of the Jenkins 'Manage Plugins' page.

## Jenkins job post-build step

If you are using a classic Jenkins 'freestyle' or similar job, add a post-build step.

## Jenkins pipeline step

Example declarative pipeline with post-build step:

    buildClerk 'https://jenkins.example.com/'

> For a full pipeline example, see `examples/jenkins-declarative-pipeline.groovy`

## Configuration

This plugin supports the following configuration options:

* `serverUrl` (required) - the base URL of the Build Clerk server

## Testing

Spin up a local Jenkins instance using:

    docker run --rm -it -p8080:8080 -v /tmp/jenkins:/var/jenkins_home jenkins/jenkins:lts
