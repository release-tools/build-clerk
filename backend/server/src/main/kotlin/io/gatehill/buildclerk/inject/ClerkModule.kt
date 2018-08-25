package io.gatehill.buildclerk.inject

import com.google.inject.AbstractModule
import io.gatehill.buildclerk.api.service.AnalysisService
import io.gatehill.buildclerk.api.service.BuildReportService
import io.gatehill.buildclerk.api.service.BuildRunnerService
import io.gatehill.buildclerk.api.service.NotificationService
import io.gatehill.buildclerk.api.service.PullRequestEventService
import io.gatehill.buildclerk.parser.Parser
import io.gatehill.buildclerk.server.Server
import io.gatehill.buildclerk.service.AnalysisServiceImpl
import io.gatehill.buildclerk.service.CommandExecutorService
import io.gatehill.buildclerk.service.PendingActionService
import io.gatehill.buildclerk.service.builder.BuildEventService
import io.gatehill.buildclerk.service.builder.BuildReportServiceImpl
import io.gatehill.buildclerk.service.builder.jenkins.JenkinsApiClientBuilder
import io.gatehill.buildclerk.service.builder.jenkins.JenkinsBuildRunnerServiceImpl
import io.gatehill.buildclerk.service.notify.slack.SlackApiService
import io.gatehill.buildclerk.service.notify.slack.SlackNotificationServiceImpl
import io.gatehill.buildclerk.service.notify.slack.SlackOperationsService
import io.gatehill.buildclerk.service.scm.ScmService
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketApiClientBuilder
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketOperationsService
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketPullRequestEventServiceImpl
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketScmServiceImpl

internal class ClerkModule : AbstractModule() {
    override fun configure() {
        bind(PendingActionService::class.java).asSingleton()
        bind(CommandExecutorService::class.java).asSingleton()

        // server
        bind(Server::class.java).asSingleton()

        // event processors
        bind(BuildReportService::class.java).to(BuildReportServiceImpl::class.java)
            .asSingleton()
        bind(BuildEventService::class.java).asSingleton()
        bind(AnalysisService::class.java).to(AnalysisServiceImpl::class.java)
            .asSingleton()

        // slack
        bind(NotificationService::class.java).to(SlackNotificationServiceImpl::class.java)
            .asSingleton()
        bind(SlackOperationsService::class.java).asSingleton()
        bind(SlackApiService::class.java).asSingleton()

        // bitbucket
        bind(ScmService::class.java).to(BitbucketScmServiceImpl::class.java).asSingleton()
        bind(BitbucketOperationsService::class.java).asSingleton()
        bind(BitbucketApiClientBuilder::class.java).asSingleton()
        bind(PullRequestEventService::class.java).to(
            BitbucketPullRequestEventServiceImpl::class.java
        )
            .asSingleton()

        // jenkins
        bind(BuildRunnerService::class.java).to(JenkinsBuildRunnerServiceImpl::class.java)
            .asSingleton()
        bind(JenkinsApiClientBuilder::class.java).asSingleton()

        // parser
        bind(Parser::class.java).asSingleton()
    }
}