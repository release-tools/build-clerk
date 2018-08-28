# Change Log

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

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
