package com.gatehill.buildbouncer.server

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.model.bitbucket.PullRequestMergedEvent
import com.gatehill.buildbouncer.model.slack.ActionTriggeredEvent
import com.gatehill.buildbouncer.service.BuildEventService
import com.gatehill.buildbouncer.service.PendingActionService
import com.gatehill.buildbouncer.service.PullRequestEventService
import com.gatehill.buildbouncer.util.jsonMapper
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

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

        server.requestHandler(router::accept).listen(Settings.Server.port)
        logger.info("Listening on http://localhost:${Settings.Server.port}")
    }

    private fun buildRouter(vertx: Vertx): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        router.get("/").handler { rc ->
            rc.response().end(homePage)
        }
        router.get("/health").handler { rc ->
            rc.response().end("ok")
        }

        router.post("/builds").consumes(JSON_CONTENT_TYPE).handler { rc ->
            val buildOutcome = try {
                rc.readBodyJson<BuildOutcome>()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(400).end("Cannot parse build outcome")
                return@handler
            }

            try {
                buildEventService.handle(buildOutcome)
                rc.response().setStatusCode(200).end()

            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.localizedMessage)
            }
        }

        router.post("/pull-requests/merged").consumes(JSON_CONTENT_TYPE).handler { rc ->
            val event = try {
                rc.readBodyJson<PullRequestMergedEvent>()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(400).end("Cannot parse webhook")
                return@handler
            }

            try {
                pullRequestEventService.verify(event)
                rc.response().setStatusCode(200).end()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.localizedMessage)
            }
        }

        // note: no 'consumes' call, as Slack sends JSON as an encoded parameter, not a raw body
        router.post("/actions").handler { rc ->
            val event = try {
                jsonMapper.readValue<ActionTriggeredEvent>(rc.request().getParam("payload"))
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(400).end("Cannot parse action")
                return@handler
            }

            try {
                val response = pendingActionService.handle(event)
                rc.response().setStatusCode(200).sendJsonResponse(response)
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.localizedMessage)
            }
        }
        return router
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
