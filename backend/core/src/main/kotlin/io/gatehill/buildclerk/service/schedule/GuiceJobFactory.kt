package io.gatehill.buildclerk.service.schedule

import com.google.inject.Injector
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import javax.inject.Inject


/**
 * Creates dependency injected Quartz jobs.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class GuiceJobFactory @Inject constructor(private val injector: Injector) : JobFactory {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        return injector.getInstance(bundle.jobDetail.jobClass)
    }
}
