GraphQL query service
=====================

Endpoint:

    http://localhost:9090/graphql

## Simple GraphQL web UI

    docker run --rm -it --name graphiql -p 4000:4000 -e API_URL=http://localhost:9090/graphql npalm/graphiql

## Sample queries

Get reports for a branch:

    {
      getReports(branchName: "master") {
        name
        build {
          number
        }
      }
    }


Get last report for a branch:

    {
      getReports(branchName: "master") {
        name
        build {
          number
        }
      }
    }
