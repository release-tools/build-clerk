Build Clerk
===========

Respond to events in your build pipeline and keep your main branch stable.

Examples:

* Auto-revert a commit after a failed build on your main branch, subject to certain thresholds and rules. 
* Notify the team when a PR was merged whilst your main branch is failing on CI.
* Lock a branch against further changes after successive build failures.

<img alt="Analysis recommends revert" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_revert.png" width="467">

<img alt="Analysis recommends lock" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_lock.png" width="467">

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

Security configuration:

* `AUTH_CONFIG_FILE` - path to [Shiro](https://shiro.apache.org) properties file for HTTP Basic authentication

Advanced configuration:

* `SERVER_PORT` - the HTTP port on which to listen

## Endpoints

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
