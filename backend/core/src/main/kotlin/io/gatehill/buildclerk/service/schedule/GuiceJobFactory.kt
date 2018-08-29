package io.gatehill.buildclerk.service.schedule

import io.gatehill.buildclerk.parser.inject.InstanceFactoryLocator
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle


/**
 *
 * @author pete
 */
class GuiceJobFactory : JobFactory {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        return InstanceFactoryLocator.instance(bundle.jobDetail.jobClass)
    }
}
