GraphQL query service
=====================

Endpoint:

    http://localhost:9090/graphql

## Simple GraphQL web UI

    docker run --rm -it --name graphiql -p 4000:4000 -e API_URL=http://localhost:9090/graphql npalm/graphiql

## Sample queries

### Build reports

List reports for a branch:

    query {
      getReports(branchName: "master") {
        name
        build {
          number
          scm {
            commit
          }
        }
      }
    }

Get last report for a branch:

    query {
      getReports(branchName: "master") {
        name
        build {
          number
          scm {
            commit
          }
        }
      }
    }

### Pull requests

Get a PR for a given commit:

    query {
      getPullRequestByCommit(commit: "coff33") {
        mergedBy: actor {
          username
        }
        pullRequest {
          title
          destination {
            branch {
              name
            }
          }
          mergeCommit {
            hash
          }
        }
      }
    }

Get last PR for a branch:

    query {
      getLastPullRequest(branchName: "master") {
        mergedBy: actor {
          username
        }
        pullRequest {
          title
          destination {
            branch {
              name
            }
          }
          mergeCommit {
            hash
          }
        }
      }
    }

List PRs for a branch:

    query {
      getPullRequests(branchName: "master") {
        mergedBy: actor {
          username
        }
        pullRequest {
          title
          destination {
            branch {
              name
            }
          }
          mergeCommit {
            hash
          }
        }
      }
    }
