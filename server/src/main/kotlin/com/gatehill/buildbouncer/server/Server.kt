package com.gatehill.buildbouncer.server

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.model.PullRequestMergedEvent
import com.gatehill.buildbouncer.model.ActionTriggeredEvent
import com.gatehill.buildbouncer.service.BuildEventService
import com.gatehill.buildbouncer.service.PendingActionService
import com.gatehill.buildbouncer.service.PullRequestEventService
import com.gatehill.buildbouncer.util.jsonMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.apache.logging.log4j.LogManager

/**
 * Listens for connections and routes requests.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Server(
        private val buildEventService: BuildEventService,
        private val pullRequestEventService: PullRequestEventService,
        private val pendingActionService: PendingActionService
) {
    private val logger = LogManager.getLogger(Server::class.java)

    fun startServer() {
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer()
        val router = buildRouter(vertx)

        server.requestHandler(router::accept).listen(9090)
        logger.info("Listening on http://localhost:9090")
    }

    private fun buildRouter(vertx: Vertx): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        router.get("/").handler { rc ->
            rc.response().end(buildHomePage())
        }
        router.get("/health").handler { rc ->
            rc.response().end("ok")
        }

        router.post("/builds").consumes("application/json").handler { rc ->
            val buildOutcome = try {
                jsonMapper.readValue<BuildOutcome>(rc.bodyAsString)
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

        router.post("/pull-requests/merged").consumes("application/json").handler { rc ->
            val event = try {
                jsonMapper.readValue<PullRequestMergedEvent>(rc.bodyAsString)
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
                rc.response().setStatusCode(400).end("Cannot parse webhook")
                return@handler
            }

            try {
                pendingActionService.handle(event)
                rc.response().setStatusCode(200).end()
            } catch (e: Exception) {
                logger.error(e)
                rc.response().setStatusCode(500).end(e.localizedMessage)
            }
        }
        return router
    }

    private fun buildHomePage(): String {
        return """
<html>
    <head>
        <title>Build Bouncer</title>
    </head>
    <body>
        <h1>Build Bouncer</h1>
        <p>
            Respond to events in your build pipeline and keep you main branch stable.
        </p>
        <p>
            Visit the project on <a href="https://github.com/outofcoffee/build-bouncer">GitHub</a>.
        </p>
    </body>
</html>
    """.trimIndent()
    }
}
