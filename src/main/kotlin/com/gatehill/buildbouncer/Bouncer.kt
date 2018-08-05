package com.gatehill.buildbouncer

import com.gatehill.buildbouncer.server.Server
import com.gatehill.buildbouncer.service.BuildAnalysisService
import com.gatehill.buildbouncer.service.BuildAnalysisServiceImpl
import com.gatehill.buildbouncer.service.BuildOutcomeService
import com.gatehill.buildbouncer.service.BuildOutcomeServiceImpl
import com.gatehill.buildbouncer.service.CommandExecutorService
import com.gatehill.buildbouncer.service.PendingActionService
import com.gatehill.buildbouncer.service.PullRequestEventService
import com.gatehill.buildbouncer.service.notify.NotificationService
import com.gatehill.buildbouncer.service.notify.StdoutNotificationServiceImpl
import com.gatehill.buildbouncer.service.runner.BuildRunnerService
import com.gatehill.buildbouncer.service.runner.jenkins.JenkinsApiClientBuilder
import com.gatehill.buildbouncer.service.runner.jenkins.JenkinsBuildRunnerServiceImpl
import com.gatehill.buildbouncer.service.scm.GitScmService
import com.gatehill.buildbouncer.service.scm.ScmService
import org.apache.logging.log4j.LogManager
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

private val logger = LogManager.getLogger("com.gatehill.buildbouncer.Bouncer")

fun main(args: Array<String>) {
    logger.info("Starting Build Bouncer")

    val kodein = Kodein {
        bind<BuildAnalysisService>() with singleton { BuildAnalysisServiceImpl(instance(), instance(), instance(), instance()) }
        bind<NotificationService>() with singleton { StdoutNotificationServiceImpl() }
        bind<PendingActionService>() with singleton { PendingActionService(instance()) }
        bind<CommandExecutorService>() with singleton { CommandExecutorService() }

        // server
        bind<Server>() with singleton { Server(instance(), instance(), instance()) }

        // event processors
        bind<BuildOutcomeService>() with singleton { BuildOutcomeServiceImpl() }
        bind<PullRequestEventService>() with singleton { PullRequestEventService(instance(), instance()) }

        // git
        bind<ScmService>() with singleton { GitScmService(instance()) }

        // jenkins
        bind<BuildRunnerService>() with singleton { JenkinsBuildRunnerServiceImpl(instance()) }
        bind<JenkinsApiClientBuilder>() with singleton { JenkinsApiClientBuilder() }
    }

    val server by kodein.instance<Server>()
    server.startServer()
}
