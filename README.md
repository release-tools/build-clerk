Build Clerk [![CircleCI](https://circleci.com/gh/outofcoffee/build-clerk.svg?style=svg)](https://circleci.com/gh/outofcoffee/build-clerk)
===========

Respond to events in your build pipeline and keep your main branch stable.

*Examples:*

* Auto-revert a commit after a failed build on your main branch, subject to certain thresholds and rules. 
* Notify the team when a PR was merged whilst your main branch is failing on CI.
* Lock a branch against further changes after successive build failures.

<img alt="Analysis recommends revert" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_revert.png" width="336">

<img alt="Analysis recommends lock" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_lock.png" width="342">

<img alt="PR merged notification" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/pr_merged.png" width="338">

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

> This is just a taste of what you can do. See the examples in the `parser` module's tests for more, or read on to learn about the events supported.

### Build events

Currently, you can react to the following events:

* `buildFailed` - a build just failed
* `branchStartsFailing` - a formerly passing branch just failed 
* `buildPassed` - a build just passed
* `branchStartsPassing` - a formerly failing branch just passed
* `repository` - repository wide rules - good for setting maximum thresholds for things like failures on a branch
* `pullRequestMerged` - a pull request was merged

### Actions

With Clerk's DSL, you can perform arbitrary logic in response to build events. Once you've figured out how you want to react, you do things like:

* revert the offending commit
* lock down the branch against further changes
* trigger a rebuild of the branch - useful to help determine if you have a flaky test
* send the above actions to a Slack channel for a human to make a decision

In addition, you can post an arbitrary message to a Slack channel of your choice, in case you need to notify people of something or cajole someone into action.

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

* `BITBUCKET_REPO_USERNAME` - the username for the Bitbucket repository's user (might be a organisation)
* `BITBUCKET_REPO_SLUG` - the 'repo slug' name for the Bitbucker repository
* `BITBUCKET_AUTH_USERNAME` - the username to authenticate with Bitbucket (if it differs from the repository username)
* `BITBUCKET_PASSWORD` - the password (preferably an 'app password') to authenticate with Bitbucket

Git repository configuration:

* `GIT_REPO_LOCAL_DIR` - path to a local repository clone
* `GIT_REPO_PUSH_CHANGES` - whether to push changes to the remote

Global filters:

* `FILTER_BRANCHES` - only process events for this SCM branch
* `FILTER_REPOS` - only process events from this repository (applies to PRs only)

Security configuration:

* `AUTH_CONFIG_FILE` - path to [Shiro](https://shiro.apache.org) properties file for HTTP Basic authentication

Advanced configuration:

* `SERVER_PORT` - the HTTP port on which to listen

### Data storage

By default, Clerk uses an in-memory data store for build reports. This will not persist between application restarts, but is good for testing/evaluation purposes.

To switch to a persistent, MongoDB based store, set the following environment variables:

    REPORT_STORE_IMPL=com.gatehill.buildclerk.dao.mongo.MongoBuildReportDaoImpl
    MONGO_HOST=localhost
    MONGO_PORT=27017

> Set the host and port variables to those of your MongoDB instance.

## Endpoints

Clerk exposes different endpoints on which it can receive different event types.

### Jenkins

Jenkins build reports, sent by either the [Notification Plugin](https://plugins.jenkins.io/notification), or the [Clerk Plugin](./jenkins-plugin) Jenkins plugins should target:

    /builds

> Example: https://clerk.example.com/builds

### Slack

Slack interactive message callbacks should target:

    /actions

> Example: https://clerk.example.com/actions

### Bitbucket

Bitbucket webhooks for the 'Pull request merged' event should target:

    /pull-requests/merged

> Example: https://clerk.example.com/pull-requests/merged

### Endpoint security

You can (and should!) set up authentication for these endpoints to prevent others from accessing your Clerk instance.

To do this, you can either use the built-in HTTP Basic Auth functionality in Clerk, or put in place an authenticating reverse proxy (out of scope of this document).

To use Clerk's HTTP Basic Auth functionality, set the `AUTH_CONFIG_FILE` environment variable to the path of a [Shiro](https://shiro.apache.org) properties file.

> Tip: for an example file, see `backend/examples/clerk-auth.properties`

When you are configuring the various third parties to call back Clerk, you can enter the callback/webhook URLs with inline credentials to match those in your Shiro properties.

For example:

    https://someuser:secretpass@clerk.example.com/actions
