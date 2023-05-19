# Build Clerk
[![CI](https://github.com/release-tools/build-clerk/actions/workflows/ci.yaml/badge.svg)](https://github.com/release-tools/build-clerk/actions/workflows/ci.yaml)

Respond to events in your build pipeline and keep your main branch stable.

*Examples:*

* Auto-revert a commit after a failed build on your main branch, subject to certain thresholds and rules. 
* Notify the team when a PR was merged whilst your main branch is failing on CI.
* Lock a branch against further changes after successive build failures.

<img alt="Analysis recommends revert" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_revert.png" width="336">

<img alt="Analysis recommends lock" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/build_analysis_lock.png" width="342">

<img alt="PR merged notification" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/pr_merged.png" width="338">

<img alt="Branch summary" src="https://github.com/outofcoffee/build-clerk/raw/master/docs/img/branch_summary.png" width="345">

## Respond to Git and CI/CD events

Clerk has a powerful DSL that lets you customise your rules:

```
if (failuresForCommitOnBranch == 1) {
    // this action will be triggered immediately
    perform action rebuildBranch()
    
} else {
    // these actions will be posted to Slack for a human decision
    suggest action revertCommit()
    suggest action lockBranch()
}

publishAnalysis("general")
```

> This is just a taste of what you can do. See the examples in the `parser` module's tests for more, or read on to learn about the events supported.

### Build events

Currently, you can react to the following events:

* `buildFailed` - a build just failed
* `branchStartsFailing` - a formerly passing branch just failed 
* `buildPassed` - a build just passed
* `branchStartsPassing` - a formerly failing branch just passed
* `repository` - repository wide rules - good for setting maximum thresholds for things like failures on a branch
* `pullRequestModified` - a pull request was created or updated
* `pullRequestMerged` - a pull request was merged
* `cron` - trigger action on a schedule - good for sending periodic summaries

### Actions

With Clerk's DSL, you can perform arbitrary logic in response to build events. Once you've figured out how you want to react, you can do things like:

* revert the offending commit
* lock down the branch against further changes
* trigger a rebuild of the branch - useful to help determine if you have a flaky test
* publish a summary of a branch's health - useful in combination with the `cron` event for sending periodic status updates
* post a comment on a pull request - helpful in conjunction with the `pullRequestModified` event for automating review steps 
* post an arbitrary message to a Slack channel, in case you need to notify people of something or cajole someone into action
* print a message to Clerk's debug log

#### Performing vs. suggesting actions

If you choose to _perform_ an action, it will be triggered immediately and a notification will be posted to Slack.

If you would prefer a human to make a decision, you can _suggest_ an action. This will result in the action being posted in Slack with buttons to confirm or dismiss it.

## Branch build notifications in Slack

Clerk can send you a direct message (DM) on Slack when an event happens to a branch you are interested in. For example, if you would like to know when a branch passes or fails a build, you can ask Clerk to notify you.

Send a message to the Clerk Slack bot as follows:

    notify me about defect-23

Clerk will now send you a DM when something happens to a branch containing the 'defect-23'. Branch name matches are case insensitive.

To list your notifications you can do this:

    list

To unsubscribe from notifications just write:

    don't notify me about defect-23

For more options, just type `help` to the bot.

## Run it

Run using Docker:

    docker run --rm -it -p9090:9090 outofcoffee/build-clerk

See http://localhost:9090 to check it's running.

## Configuration options

* `RULES_FILE` (required) - path to rules file to respond to build lifecycle events

### Slack configuration

* `SLACK_USER_TOKEN` - a token with the permissions below

The Slack token should have the appropriate permissions if you want to enable the respective Clerk feature:

| Permission                | Feature                                       |
|---------------------------|-----------------------------------------------|
| `chat:write:bot`          | Mandatory. Ability to post to channels.       |

If you wish to allow users to subscribe to notifications via IM, you must:

* add a bot user
* add the `message.im` event subscription

### Jenkins configuration

* `JENKINS_BASE_URL` - base URL for Jenkins server
* `JENKINS_USERNAME` - username for Jenkins
* `JENKINS_PASSWORD` - API key or password for Jenkins

### Bitbucket configuration

* `BITBUCKET_REPO_USERNAME` - the username for the Bitbucket repository's user (might be a organisation)
* `BITBUCKET_REPO_SLUG` - the 'repo slug' name for the Bitbucket repository
* `BITBUCKET_AUTH_USERNAME` - the username to authenticate with Bitbucket (if it differs from the repository username)
* `BITBUCKET_PASSWORD` - the password (preferably an 'app password') to authenticate with Bitbucket

The Bitbucket user should have the appropriate permissions if you want to enable the respective Clerk feature:

| Permission                | Feature                        |
|---------------------------|--------------------------------|
| Pull request Read/Write   | Add comments to PRs.           |
| Repository Admin          | Lock branch access.            |

### Global filters

* `FILTER_BRANCHES` - only process events for this SCM branch
* `FILTER_REPOS` - only process events from this repository (applies to PRs only)

### Security configuration

* `AUTH_CONFIG_FILE` - path to [Shiro](https://shiro.apache.org) properties file for HTTP Basic authentication

### Advanced configuration

* `SERVER_PORT` - the HTTP port on which to listen
* `RULES_PARSE_ON_STARTUP` - whether to parse the rules file on startup - useful to fail fast if configuration is broken

### Data storage

By default, Clerk uses an in-memory data store for build reports. This will not persist between application restarts, but is good for testing/evaluation purposes.

To switch to a persistent, MongoDB based store, set the following environment variables:

    STORE_IMPL=mongo
    MONGO_HOST=localhost
    MONGO_PORT=27017

> Set the host and port variables to those of your MongoDB instance.

### Understanding your source repository

For some analyses, Clerk can examine the commits in your source repository.

In order for this to work, Clerk must have access to a bare Git repository, or be authenticated to clone one.

The relevant configuration variables for working with the source repository are:

* `GIT_REPO_LOCAL_DIR` - path to a local repository clone
* `GIT_REPO_REMOTE_URL` - the remote URL of the repository.
* `GIT_REPO_USERNAME` - used for HTTP(S) remote repository URLs only
* `GIT_REPO_PASSWORD` - used for HTTP(S) remote repository URLs, or, optionally with SSH URLs where a public key is not used

If you choose to use the `revertCommit` action, you'll also need to set the following configuration variable:

* `GIT_REPO_PUSH_CHANGES` (default: `false`) - whether to push changes to the remote

> If this is set to `false`, any changes will not be pushed to the remote repository. This can be useful for testing.

#### A note on Git authentication

For SSH URLs, Clerk uses the configuration of the machine on which it is running. This means your local SSH configuration (public key etc.) is used.

If you need to override SSH strict host key checking when working with remote repositories, set the following:

* `GIT_REPO_STRICT_HOST_KEY_CHECKING` (boolean)

For HTTP(S) URLs, Clerk looks for the configured username and password, or else assumes the remote repository is unauthenticated.

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

Slack event API requests should target:

    /messages

> Example: https://clerk.example.com/messages

### Bitbucket

Bitbucket webhooks for Pull Request events should target:

    /pull-requests

> Example: https://clerk.example.com/pull-requests

### Utility endpoints

* `/` - root page shows project information
* `/health` - returns HTTP 200 when server is up - useful for healthchecks 
* `/stats` - shows statistics. Requires authentication if endpoint security is enabled. 

### Endpoint security

You can (and should!) set up authentication for these endpoints to prevent others from accessing your Clerk instance.

To do this, you can either use the built-in HTTP Basic Auth functionality in Clerk, or put in place an authenticating reverse proxy (out of scope of this document).

To use Clerk's HTTP Basic Auth functionality, set the `AUTH_CONFIG_FILE` environment variable to the path of a [Shiro](https://shiro.apache.org) properties file.

> Tip: for an example file, see `backend/examples/clerk-auth.properties`

When you are configuring the various third parties to call back Clerk, you can enter the callback/webhook URLs with inline credentials to match those in your Shiro properties.

For example:

    https://someuser:secretpass@clerk.example.com/actions

### Querying data

Data held by Clerk can be queried via its GraphQL endpoint:

    /graphql

From this, you can fetch information about builds and pull requests, as well as performing analyses on them.

For more details, see the [query service](./backend/query) docs.
