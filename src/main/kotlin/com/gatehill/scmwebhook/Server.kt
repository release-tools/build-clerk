package com.gatehill.scmwebhook

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.scmwebhook.model.BuildOutcome
import com.gatehill.scmwebhook.model.PullRequestMergedEvent
import com.gatehill.scmwebhook.service.BranchStatusService
import com.gatehill.scmwebhook.service.BuildAnalysisService
import com.gatehill.scmwebhook.service.PullRequestEventService
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.apache.logging.log4j.LogManager
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

private val logger = LogManager.getLogger("com.gatehill.scmwebhook.Server")
private val jsonMapper by lazy { ObjectMapper().registerKotlinModule() }

fun main(args: Array<String>) {
    logger.info("Starting SCM webhook receiver")

    val kodein = Kodein {
        bind<BranchStatusService>() with singleton { BranchStatusService(instance()) }
        bind<PullRequestEventService>() with singleton { PullRequestEventService(instance()) }
        bind<BuildAnalysisService>() with singleton { BuildAnalysisService() }
    }

    startServer(kodein)
}

private fun startServer(kodein: Kodein) {
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()
    val router = buildRouter(vertx, kodein)

    server.requestHandler({ router.accept(it) }).listen(9090)
    logger.info("Listening on http://localhost:9090")
}

private fun buildRouter(vertx: Vertx, kodein: Kodein): Router {
    val router = Router.router(vertx)
    val branchStatusService: BranchStatusService by kodein.instance()
    val pullRequestEventService: PullRequestEventService by kodein.instance()

    router.route().handler(BodyHandler.create())

    router.get("/").handler { rc ->
        rc.response().end("SCM webhook receiver")
    }

    router.post("/builds").consumes("application/json").handler { rc ->
        val buildOutcome = try {
            jsonMapper.readValue<BuildOutcome>(rc.bodyAsString)
        } catch (e: Exception) {
            logger.error(e)
            rc.response().setStatusCode(400).end("Cannot parse branch status")
            return@handler
        }

        try {
            branchStatusService.updateStatus(buildOutcome)
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
