package io.gatehill.buildclerk.inject

import com.google.inject.AbstractModule
import io.gatehill.buildclerk.parser.Parser
import io.gatehill.buildclerk.service.schedule.ScheduledTaskService

internal class DaemonModule : AbstractModule() {
    override fun configure() {
        // cron scheduled tasks
        bind(ScheduledTaskService::class.java).asEagerSingleton()

        // parser
        bind(Parser::class.java).asEagerSingleton()
    }
}
