package hu.dlaszlo.vsha.device

import org.slf4j.LoggerFactory
import org.springframework.scheduling.support.CronSequenceGenerator
import java.util.*

class Scheduler(
    val deviceId: String,
    val scheduleType: ScheduleType,
    var timeout: Int?,
    val cronDefinition: String?,
    val action: Action
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
            logger.info("deviceId: {}, actionId: {}, nextRun = {}", deviceId, action.id, Date(nextRun!!), Date())
        }
    }

    fun action(): Boolean {
        var remove = false
        if (scheduleType == ScheduleType.Immediate) {
            (action.handler!!)()
            remove = true
        } else if (scheduleType == ScheduleType.Timeout) {
            if (nextRun!! <= System.currentTimeMillis()) {
                action.handler!!.invoke()
                remove = true
            }
        } else if (scheduleType == ScheduleType.FixedRate) {
            if (nextRun!! <= System.currentTimeMillis()) {
                nextRun = nextRun!! + timeout!!
                action.handler!!.invoke()
            }
        } else if (scheduleType == ScheduleType.CronScheduler) {
            if (nextRun!! <= System.currentTimeMillis()) {
                val currentDate = cronSequenceGenerator!!.next(previousDate!!)
                nextRun = currentDate.time
                previousDate = currentDate
                action.handler!!.invoke()
                logger.info("deviceId: {}, actionId: {}, nextRun = {}", deviceId, action.id, Date(nextRun!!), Date())
            }
        }
        return remove
    }

    companion object {
        val logger = LoggerFactory.getLogger(Scheduler::class.java)!!
    }

}
