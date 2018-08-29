package io.gatehill.buildclerk.inject

import com.google.inject.AbstractModule
import io.gatehill.buildclerk.service.schedule.ScheduledTaskService

internal class DaemonModule : AbstractModule() {
    override fun configure() {
        bind(ScheduledTaskService::class.java).asEagerSingleton()
    }
}
