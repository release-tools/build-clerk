package com.gatehill.buildclerk.server

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.buildclerk.api.model.BuildReport
import com.gatehill.buildclerk.api.model.PullRequestMergedEvent
import com.gatehill.buildclerk.config.Settings
import com.gatehill.buildclerk.model.slack.ActionTriggeredEvent
import com.gatehill.buildclerk.service.PendingActionService
import com.gatehill.buildclerk.service.builder.BuildEventService
import com.gatehill.buildclerk.service.scm.PullRequestEventService
import com.gatehill.buildclerk.util.jsonMapper
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.auth.shiro.PropertiesProviderConstants.PROPERTIES_PROPS_PATH_FIELD
import io.vertx.ext.auth.shiro.ShiroAuth
import io.vertx.ext.auth.shiro.ShiroAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.apache.logging.log4j.LogManager
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Listens for connections and routes requests.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Server @Inject constructor(
        private val buildEventService: BuildEventService,
        private val pullRequestEventService: PullRequestEventService,
        private val pendingActionService: PendingActionService
) {
    private val logger = LogManager.getLogger(Server::class.java)

    private val homePage by lazy {
        Server::class.java.getResourceAsStream("/index.html").bufferedReader().use { it.readText() }
    }

    fun startServer() {
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer()
        val router = buildRouter(vertx)

        server.requestHandler(router::accept).listen(Settings.Server.port) { event ->
            if (event.succeeded()) {
                logger.info("Listening on http://localhost:${Settings.Server.port}")
            } else {
                logger.error("Error listening on port ${Settings.Server.port}", event.cause())
                vertx.close { exitProcess(1) }
            }
        }
    }

    private fun buildRouter(vertx: Vertx) = Router.router(vertx).apply {
        route().handler(BodyHandler.create())
        configureAuth(vertx, this)

        get("/").handler { rc ->
            rc.response().end(homePage)
        }
        get("/health").handler { rc ->
            rc.response().end("ok")
        }

        post("/builds").consumes(JSON_CONTENT_TYPE).handler { rc ->
            val buildReport = try {
                rc.readBodyJson<BuildReport>()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(400).end("Cannot parse build report. ${e.message}")
                return@handler
            }

            try {
                buildEventService.checkBuildReport(buildReport)
                rc.response().setStatusCode(200).end()

            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.message)
            }
        }

        post("/pull-requests/merged").consumes(JSON_CONTENT_TYPE).handler { rc ->
            val event = try {
                rc.readBodyJson<PullRequestMergedEvent>()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(400).end("Cannot parse webhook. ${e.message}")
                return@handler
            }

            try {
                pullRequestEventService.checkPullRequest(event)
                rc.response().setStatusCode(200).end()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.message)
            }
        }

        // note: no 'consumes' call, as Slack sends JSON as an encoded parameter, not a raw body
        post("/actions").handler { rc ->
            val event = try {
                jsonMapper.readValue<ActionTriggeredEvent>(rc.request().getParam("payload"))
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(400).end("Cannot parse action. ${e.message}")
                return@handler
            }

            try {
                pendingActionService.handleAsync(event)
                rc.response().setStatusCode(200).end()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.message)
            }
        }
    }

    /**
     * Add an auth handler if security properties are configured.
     */
    private fun configureAuth(vertx: Vertx, router: Router) {
        Settings.Auth.configFile?.let { authConfigFile ->
            logger.info("Configuring security from $authConfigFile")
            val authProvider = ShiroAuth.create(vertx, ShiroAuthOptions().apply {
                config = json {
                    obj(PROPERTIES_PROPS_PATH_FIELD to authConfigFile)
                }
            })

            val authHandler = BasicAuthHandler.create(authProvider)
            router.route().handler { rc ->
                when (rc.request().uri()) {
                    in "/", "/health" -> rc.next()
                    else -> authHandler.handle(rc)
                }
            }

        } ?: logger.warn("No security is configured! All endpoints are exposed without authentication. Set AUTH_CONFIG_FILE to a valid Shiro properties file.")
    }


    private inline fun <reified T : Any> RoutingContext.readBodyJson(): T = jsonMapper.readValue(bodyAsString)

    private fun HttpServerResponse.sendJsonResponse(response: Any) {
        putHeader("Content-Type", JSON_CONTENT_TYPE)
        end(jsonMapper.writeValueAsString(response))
    }

    companion object {
        private const val JSON_CONTENT_TYPE = "application/json"
    }
}
