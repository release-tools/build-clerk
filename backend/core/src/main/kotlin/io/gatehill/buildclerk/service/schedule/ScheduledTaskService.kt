package io.gatehill.buildclerk.service.schedule

import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.dsl.CronBlock
import io.gatehill.buildclerk.parser.Parser
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.JobBuilder.newJob
import org.quartz.JobKey
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject


/**
 * Runs configured blocks at specified times.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ScheduledTaskService @Inject constructor(
    private val parser: Parser,
    private val guiceJobFactory: GuiceJobFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(ScheduledTaskService::class.java)
    private val tasks = mutableMapOf<String, CronBlock.() -> Unit>()

    private val scheduler = StdSchedulerFactory.getDefaultScheduler().apply {
        setJobFactory(guiceJobFactory)
        start()
    }

    init {
        scheduleConfigRefresh()
    }

    private fun scheduleConfigRefresh() {
        val job = newJob(ConfigReloadJob::class.java).build()

        val trigger = newTrigger()
            .withSchedule(simpleSchedule().withIntervalInMinutes(5).repeatForever())
            .build()

        scheduler.scheduleJob(job, trigger)
    }

    fun initialiseTasks() {
        scheduler.clear()
        tasks.clear()

        val config = parser.parse(Settings.Rules.configFile)
        logger.debug("Initialising ${config.scheduledTasks.size} scheduled task(s)")

        config.scheduledTasks.forEach { scheduledTask ->
            val taskId = UUID.randomUUID().toString()
            tasks += taskId to scheduledTask.second

            val job = newJob(CronJob::class.java)
                .withIdentity(taskId)
                .build()

            val trigger = newTrigger()
                .withSchedule(cronSchedule(scheduledTask.first))
                .build()

            scheduler.scheduleJob(job, trigger)
        }
    }

    fun runTask(taskId: String) {
        logger.debug("Searching for task with ID: $taskId")

        tasks[taskId]?.let { task ->
            logger.debug("Invoking task with ID: $taskId")
            parser.invoke(
                body = task
            )

        } ?: run {
            logger.warn("Unable to find task with ID: $taskId - removing from scheduler")
            scheduler.deleteJob(JobKey.jobKey(taskId))
        }
    }
}
