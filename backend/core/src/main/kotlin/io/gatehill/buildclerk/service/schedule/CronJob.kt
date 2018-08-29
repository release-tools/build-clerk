package io.gatehill.buildclerk.service.schedule

import org.quartz.Job
import org.quartz.JobExecutionContext
import javax.inject.Inject

class CronJob @Inject constructor(
    private val scheduledTaskService: ScheduledTaskService
) : Job {
    override fun execute(context: JobExecutionContext) {
        scheduledTaskService.runTask(context.jobDetail.key.name)
    }
}
