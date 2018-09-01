package io.gatehill.buildclerk.query

import com.github.pgutkowski.kgraphql.KGraphQL
import com.github.pgutkowski.kgraphql.schema.Schema
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.query.model.Query
import javax.inject.Inject

/**
 * GraphQL query service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class QueryService @Inject constructor(
    private val buildReportService: BuildReportService
) {
    private val schema: Schema = KGraphQL.schema {

        // configure method allows you customize schema behaviour
        configure {
            useDefaultPrettyPrinter = true
        }

        query("getLastReport") {
            resolver { branchName: String ->
                buildReportService.fetchLastBuildForBranch(branchName)
            }
        }

        query("getReports") {
            resolver { branchName: String ->
                buildReportService.fetchReportsForBranch(branchName)
            }
        }

        //kotlin classes need to be registered with "type" method
        //to be included in created schema type system
        //return types of queries are automatically included
        type<BuildReport>()
        enum<BuildStatus>()
    }

    fun query(query: Query) = schema.execute(query.query, query.variables)
}
