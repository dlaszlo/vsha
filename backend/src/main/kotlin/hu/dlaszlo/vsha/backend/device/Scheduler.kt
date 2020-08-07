package hu.dlaszlo.vsha.backend.device

import org.slf4j.LoggerFactory
import org.springframework.scheduling.support.CronSequenceGenerator
import java.util.*

class Scheduler<T : AbstractDeviceConfig>(
    val device: T,
    val action: (t: T) -> Boolean,
    val scheduleType: ScheduleType,
    var timeout: Long?,
    val cronDefinition: String?
) {

    private var nextRun: Long? = null
    private var cronSequenceGenerator: CronSequenceGenerator? = null
    private var previousDate: Date? = null

    init {
        if (scheduleType == ScheduleType.Immediate) {
            nextRun = 0
        } else if (scheduleType == ScheduleType.Timeout
            || scheduleType == ScheduleType.FixedRate
        ) {
            nextRun = System.currentTimeMillis() + timeout!!
        } else if (scheduleType == ScheduleType.CronScheduler) {
            previousDate = Date()
            cronSequenceGenerator = CronSequenceGenerator(cronDefinition!!)
            val currentDate = cronSequenceGenerator!!.next(previousDate!!)
            nextRun = currentDate.time
            previousDate = currentDate
            logger.info("device = {}, action = {}, nextRun = {}", device.javaClass.canonicalName, action, Date(nextRun!!), Date())
        }
    }

    fun runAction(): Boolean {
        var remove = false
        if (scheduleType == ScheduleType.Immediate) {
            action(device)
            remove = true
        } else if (scheduleType == ScheduleType.Timeout) {
            if (nextRun!! <= System.currentTimeMillis()) {
                action(device)
                remove = true
            }
        } else if (scheduleType == ScheduleType.FixedRate) {
            if (nextRun!! <= System.currentTimeMillis()) {
                nextRun = nextRun!! + timeout!!
                action(device)
            }
        } else if (scheduleType == ScheduleType.CronScheduler) {
            if (nextRun!! <= System.currentTimeMillis()) {
                val currentDate = cronSequenceGenerator!!.next(previousDate!!)
                nextRun = currentDate.time
                previousDate = currentDate
                action(device)
                logger.info("device = {}, action = {}, nextRun = {}", device.javaClass.canonicalName, action, Date(nextRun!!), Date())
            }
        }
        return remove
    }

    companion object {
        val logger = LoggerFactory.getLogger(Scheduler::class.java)!!
    }

}
