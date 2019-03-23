# Change Log

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.7.0] - 2019-04-02
### Added
- Adds the ability for users to request and receive branch notifications via Slack DM.

## [0.6.7] - 2018-09-18
### Added
- Improves diffstat logging.
- Improves release scripts.

### Fixed
- Improves null safety when parsing for diffstat responses.

## [0.6.6] - 2018-09-18
### Added
- Adds `debug` log function to DSL.

### Changed
- Switches to using diffstat to obtain list of changed files in a PR (fixes #23).
- Disables following HTTP(S) redirects when invoking third party APIs.

## [0.6.5] - 2018-09-17
### Added
- Embeds version stamp in manifest and prints it to startup log and home page.

### Fixed
- Don't trigger build status transition event if there is a newer build already recorded (fixes #22).
- Fixes build report branch filter in MongoDB store.

## [0.6.4] - 2018-09-11
### Fixed
- Fixes date range build report query.

## [0.6.3] - 2018-09-10
### Fixed
- Fixes posting PR comments.

## [0.6.2] - 2018-09-10
### Fixed
- Allows file diff to use partial Git hash.

## [0.6.1] - 2018-09-10
### Fixed
- Fixes logging of exception stack traces.

## [0.6.0] - 2018-09-07
### Added
- Allows PR branch changes to be queried. New DSL block `pullRequestModified` introduced.
- Enables comments to be added to a PR. New DSL function `ensureComment(message)` introduced.

### Changed
- Modifies inbound PR webhook endpoint.

## [0.5.5] - 2018-09-04
### Added
- Adds pass rate to branch summary.

## [0.5.4] - 2018-09-04
### Fixed
- Fix branch summary line padding.

## [0.5.3] - 2018-09-03
### Fixed
- Normalises PR commit hash length for storage and queries.

## [0.5.2] - 2018-09-03
### Added
- Allows credentials to be specified for MongoDB store.
- Sets JSON content type on GraphQL endpoint.
- Fixes successful/failed count in branch analysis query.

## [0.5.1] - 2018-09-03
### Added
- Improves branch summary.
- Improves Analysis description.

### Fixed
- Fixes build report ordering to use build number instead of date report received.

## [0.5.0] - 2018-09-03
### Added
- Adds GraphQL endpoint for analysing build reports.
- Adds GraphQL endpoint for pull requests.
- Adds GraphQL endpoint for build reports.

## [0.4.0] - 2018-08-31
### Added
- Adds scheduled events support to DSL.
- Allows actions to be performed immediately or suggested for human interaction.
- Adds branch summary function to DSL.

### Changed
- Parse configuration rules on startup (enabled by default).

## [0.3.5] - 2018-08-30
### Added
- Adds earliest and newest record timestamps to statistics endpoint.
- Adds 'commit' property to DSL.

# Changed
- Ignores event keys that are not PR merges.

## [0.3.4] - 2018-08-28
### Added
- Allows build status to be set via DSL (fixes #17).

## [0.3.3] - 2018-08-28
### Added
- Sets SSH transport configuration on all remote operations.

## [0.3.2] - 2018-08-28
### Added
- Improves logging for SSH host key checking.

## [0.3.1] - 2018-08-28
### Added
- Allows override of SSH strict host key checking when working with remote repositories.

## [0.3.0] - 2018-08-28
### Added
- Adds support for cloning remote Git repositories (fixes #2).
- Fetches committer and author identities for analysis (fixes #16).
- Adds statistics endpoint listing object counts.
- Improves DSL for publishing analysis and posting messages.
- Improves SCM clone documentation.

### Changed
- Only removes local repository directory on cloning if already exists.

## [0.2.0] - 2018-08-26
### Added
- Adds Changelog (this file)
- Adds Mongo implementation of for pending actions (fixes #11).
- Adds ServiceLoader powered plugin locator framework.

### Changed
- Aligns package name with domain.

## [0.1.2] - 2018-08-25
### Added
- Adds MongoDB implementation of pull request event DAO.
- Adds analysis entry and DSL property exposing number of passed builds for a commit (fixes #14).

## [0.1.1] - 2018-08-23
### Added
- Adds persistence layer for pending actions. Using in-memory implementation for now.
- Adds persistence layer for pull requests. Using in-memory implementation for now.
- Looks up pull request for a commit (fixes #13).
- Improves release scripts.

## [0.1.0] - 2018-18-23
### Added
- Adds support for exclusive actions.
- Allows branch and repo names to be regular expressions.
- Improves test coverage.

### Fixed
- Ensures analysis is posted either exactly once (or never).
