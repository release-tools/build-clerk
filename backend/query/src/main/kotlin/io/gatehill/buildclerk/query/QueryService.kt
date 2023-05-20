package io.gatehill.buildclerk.query

import com.apurebase.kgraphql.KGraphQL
import com.apurebase.kgraphql.schema.Schema
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import io.gatehill.buildclerk.api.model.BuildDetails
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.pr.Commit
import io.gatehill.buildclerk.api.model.pr.PullRequest
import io.gatehill.buildclerk.api.model.pr.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.pr.RepoBranch
import io.gatehill.buildclerk.api.model.ReportSpan
import io.gatehill.buildclerk.api.model.pr.Repository
import io.gatehill.buildclerk.api.model.Scm
import io.gatehill.buildclerk.api.model.pr.User
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.query.model.Query
import io.gatehill.buildclerk.supervisedDefaultCoroutineScope
import org.apache.logging.log4j.LogManager
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

/**
 * GraphQL query service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class QueryService @Inject constructor(
    private val buildReportService: BuildReportService,
    private val pullRequestEventService: PullRequestEventService,
    private val analysisService: AnalysisService
) : CoroutineScope by supervisedDefaultCoroutineScope {
    private val logger = LogManager.getLogger(QueryService::class.java)

    private val schema: Schema = KGraphQL.schema {

        // configure method allows you customize schema behaviour
        configure {
            useDefaultPrettyPrinter = true
        }

        addBuildReportQueries()
        addPullRequestQueries()
        addAnalysisQueries()

        // workaround for GraphiQL bug: https://github.com/pgutkowski/KGraphQL/issues/17
        mutation("doNothing") {
            description = "Does nothing"
            resolver { -> "noop" }
        }
    }

    private fun SchemaBuilder.addBuildReportQueries() {
        query("getLastReport") {
            resolver { branchName: String? ->
                buildReportService.fetchLastReport(branchName)
            }
        }

        query("getReports") {
            resolver { branchName: String? ->
                buildReportService.fetchReports(branchName)
            }
        }

        //kotlin classes need to be registered with "type" method
        //to be included in created schema type system
        //return types of queries are automatically included
        type<BuildReport>()
        type<BuildDetails>()
        type<Scm>()
        enum<BuildStatus>()
    }

    private fun SchemaBuilder.addPullRequestQueries() {
        query("getPullRequestByCommit") {
            resolver { commit: String ->
                pullRequestEventService.findPullRequestByMergeCommit(commit)
            }
        }

        query("getLastPullRequest") {
            resolver { branchName: String? ->
                pullRequestEventService.fetchLastPullRequest(branchName)
            }
        }

        query("getPullRequests") {
            resolver { branchName: String? ->
                pullRequestEventService.fetchPullRequests(branchName)
            }
        }

        //kotlin classes need to be registered with "type" method
        //to be included in created schema type system
        //return types of queries are automatically included
        type<PullRequestMergedEvent>()
        type<PullRequest>()
        type<Repository>()
        type<User>()
        type<RepoBranch>()
        type<Commit>()
    }

    private fun SchemaBuilder.addAnalysisQueries() {
        query("analyseReports") {
            resolver { branchName: String?, start: String, end: String ->
                analysisService.analyseReportSpan(
                    branchName = branchName,
                    start = ZonedDateTime.parse(start),
                    end = ZonedDateTime.parse(end)
                )
            }
        }

        type<ReportSpan>()
    }

    fun query(query: Query): String {
        logger.debug("Executing GraphQL query")
        return runBlocking {
            schema.execute(query.query, query.variables)
        }
    }
}
