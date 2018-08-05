package com.gatehill.buildbouncer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.buildbouncer.model.BuildOutcome
import com.gatehill.buildbouncer.model.PullRequestMergedEvent
import com.gatehill.buildbouncer.service.BuildAnalysisService
import com.gatehill.buildbouncer.service.BuildOutcomeService
import com.gatehill.buildbouncer.service.BuildRunnerService
import com.gatehill.buildbouncer.service.NotificationService
import com.gatehill.buildbouncer.service.PullRequestEventService
import com.gatehill.buildbouncer.service.ScmService
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.apache.logging.log4j.LogManager
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

private val logger = LogManager.getLogger("com.gatehill.buildbouncer.Server")
private val jsonMapper by lazy { ObjectMapper().registerKotlinModule() }

fun main(args: Array<String>) {
    logger.info("Starting Build Bouncer")

    val kodein = Kodein {
        bind<BuildOutcomeService>() with singleton { BuildOutcomeService(instance()) }
        bind<PullRequestEventService>() with singleton { PullRequestEventService(instance(), instance()) }
        bind<BuildAnalysisService>() with singleton { BuildAnalysisService(instance(), instance(), instance(), instance()) }
        bind<BuildRunnerService>() with singleton { BuildRunnerService() }
        bind<ScmService>() with singleton { ScmService(instance()) }
        bind<NotificationService>() with singleton { NotificationService() }
    }

    startServer(kodein)
}

private fun startServer(kodein: Kodein) {
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()
    val router = buildRouter(vertx, kodein)

    server.requestHandler(router::accept).listen(9090)
    logger.info("Listening on http://localhost:9090")
}

private fun buildRouter(vertx: Vertx, kodein: Kodein): Router {
    val router = Router.router(vertx)
    val buildOutcomeService: BuildOutcomeService by kodein.instance()
    val pullRequestEventService: PullRequestEventService by kodein.instance()

    router.route().handler(BodyHandler.create())

    router.get("/").handler { rc ->
        rc.response().end("""
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
""".trimIndent())
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
            buildOutcomeService.updateStatus(buildOutcome)
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
    return router
}
