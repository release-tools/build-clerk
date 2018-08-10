package com.gatehill.buildbouncer

import com.gatehill.buildbouncer.api.service.BuildOutcomeService
import com.gatehill.buildbouncer.api.service.BuildRunnerService
import com.gatehill.buildbouncer.api.service.NotificationService
import com.gatehill.buildbouncer.parser.Parser
import com.gatehill.buildbouncer.parser.inject.InstanceFactory
import com.gatehill.buildbouncer.parser.inject.InstanceFactoryLocator
import com.gatehill.buildbouncer.server.Server
import com.gatehill.buildbouncer.service.BuildAnalysisService
import com.gatehill.buildbouncer.service.BuildEventService
import com.gatehill.buildbouncer.service.BuildOutcomeServiceImpl
import com.gatehill.buildbouncer.service.CommandExecutorService
import com.gatehill.buildbouncer.service.PendingActionService
import com.gatehill.buildbouncer.service.PullRequestEventService
import com.gatehill.buildbouncer.service.notify.slack.SlackApiService
import com.gatehill.buildbouncer.service.notify.slack.SlackNotificationServiceImpl
import com.gatehill.buildbouncer.service.notify.slack.SlackOperationsService
import com.gatehill.buildbouncer.service.runner.jenkins.JenkinsApiClientBuilder
import com.gatehill.buildbouncer.service.runner.jenkins.JenkinsBuildRunnerServiceImpl
import com.gatehill.buildbouncer.service.scm.ScmService
import com.gatehill.buildbouncer.service.scm.bitbucket.BitbucketApiClientBuilder
import com.gatehill.buildbouncer.service.scm.bitbucket.BitbucketOperationsService
import com.gatehill.buildbouncer.service.scm.bitbucket.BitbucketScmServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Singleton
import com.google.inject.binder.ScopedBindingBuilder
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger("com.gatehill.buildbouncer.Bouncer")

fun main(args: Array<String>) {
    logger.info("Starting Build Bouncer")

    val injector = Guice.createInjector(object : AbstractModule() {
        override fun configure() {
            bind(PendingActionService::class.java).asSingleton()
            bind(CommandExecutorService::class.java).asSingleton()

            // server
            bind(Server::class.java).asSingleton()

            // event processors
            bind(BuildOutcomeService::class.java).to(BuildOutcomeServiceImpl::class.java).asSingleton()
            bind(PullRequestEventService::class.java).asSingleton()
            bind(BuildAnalysisService::class.java).asSingleton()
            bind(BuildEventService::class.java).asSingleton()

            // slack
            bind(NotificationService::class.java).to(SlackNotificationServiceImpl::class.java).asSingleton()
            bind(SlackOperationsService::class.java).asSingleton()
            bind(SlackApiService::class.java).asSingleton()

            // bitbucket
            bind(ScmService::class.java).to(BitbucketScmServiceImpl::class.java).asSingleton()
            bind(BitbucketOperationsService::class.java).asSingleton()
            bind(BitbucketApiClientBuilder::class.java).asSingleton()

            // jenkins
            bind(BuildRunnerService::class.java).to(JenkinsBuildRunnerServiceImpl::class.java).asSingleton()
            bind(JenkinsApiClientBuilder::class.java).asSingleton()

            // parser
            bind(Parser::class.java).asSingleton()
        }
    })

    InstanceFactoryLocator.instanceFactory = object : InstanceFactory {
        override fun <T : Any> instance(clazz: Class<T>) = injector.getInstance(clazz)
    }

    val server = injector.getInstance(Server::class.java)
    server.startServer()
}

/**
 * Syntactic sugar for binding Guice singletons.
 */
private fun ScopedBindingBuilder.asSingleton() = this.`in`(Singleton::class.java)
