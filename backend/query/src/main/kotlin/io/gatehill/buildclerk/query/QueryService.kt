package io.gatehill.buildclerk.query

import com.github.pgutkowski.kgraphql.KGraphQL
import com.github.pgutkowski.kgraphql.schema.Schema
import com.github.pgutkowski.kgraphql.schema.dsl.SchemaBuilder
import io.gatehill.buildclerk.api.model.BuildDetails
import io.gatehill.buildclerk.api.model.BuildReport
import io.gatehill.buildclerk.api.model.BuildStatus
import io.gatehill.buildclerk.api.model.Commit
import io.gatehill.buildclerk.api.model.PullRequest
import io.gatehill.buildclerk.api.model.PullRequestMergedEvent
import io.gatehill.buildclerk.api.model.RepoBranch
import io.gatehill.buildclerk.api.model.Repository
import io.gatehill.buildclerk.api.model.Scm
import io.gatehill.buildclerk.api.model.User
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.query.model.Query
import io.gatehill.buildclerk.query.model.ReportSpan
import org.apache.logging.log4j.LogManager
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * GraphQL query service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class QueryService @Inject constructor(
    private val buildReportService: BuildReportService,
    private val pullRequestEventService: PullRequestEventService
) {
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

    private fun SchemaBuilder<Unit>.addBuildReportQueries() {
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

    private fun SchemaBuilder<Unit>.addPullRequestQueries() {
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

    private fun SchemaBuilder<Unit>.addAnalysisQueries() {
        query("analyseReports") {
            resolver { branchName: String?, start: String, end: String ->
                buildReportService.fetchReportsBetween(
                    branchName,
                    toZonedDateTimeUtc(start),
                    toZonedDateTimeUtc(end)
                ).let { reports ->
                    val reportCount = reports.size
                    val successful = reports.count { it.build.status == BuildStatus.FAILED }
                    val failed = reports.count { it.build.status == BuildStatus.SUCCESS }

                    ReportSpan(
                        dataPoints = reportCount,
                        successful = successful,
                        failed = failed,
                        passRate = when (reportCount) {
                            0 -> 0.toDouble()
                            else -> successful / reportCount.toDouble()
                        }
                    )
                }
            }
        }

        type<ReportSpan>()
    }

    fun query(query: Query): String {
        logger.debug("Executing GraphQL query")
        return schema.execute(query.query, query.variables)
    }
}

private fun toZonedDateTimeUtc(input: String): ZonedDateTime = ZonedDateTime.parse(input)
