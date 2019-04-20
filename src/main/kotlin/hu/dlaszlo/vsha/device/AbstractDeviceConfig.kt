package hu.dlaszlo.vsha.device

import hu.dlaszlo.vsha.mqtt.Mqtt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

abstract class AbstractDeviceConfig {

    @Autowired
    private lateinit var mqtt: Mqtt

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    abstract var device: Device

    val schedulerList = mutableListOf<Scheduler>()

    protected fun publish(topic: String, payload: String, retained: Boolean) {
        mqtt.publish(topic, payload, retained)
    }

    fun device(dev: Device.() -> Unit): Device = Device().apply(dev)

    fun Device.initialize(init: () -> Unit) {
        this.initialize = init
    }

    fun Device.route(route: Route.() -> Unit) {
        routeList.add(Route().apply(route))
    }

    fun Device.action(action: Action.() -> Unit) {
        actionList.add(Action().apply(action))
    }

    private fun getAction(deviceId: String, actionId: String): Action {
        val deviceConfig: AbstractDeviceConfig = applicationContext.getBean(deviceId, AbstractDeviceConfig::class.java)
        var ret: Action? = null
        for (action in deviceConfig.device.actionList) {
            if (actionId.equals(action.id, true)) {
                ret = action
            }
        }
        return ret!!
    }

    fun actionNow(deviceId: String, actionId: String) {
        var found = false
        for (scheduler in schedulerList) {
            if (scheduler.callerDeviceId == device.deviceId
                && scheduler.deviceId == deviceId
                && scheduler.action.id == actionId
                && scheduler.scheduleType == ScheduleType.Immediate
            ) {
                found = true
                break
            }
        }
        if (!found) {
            val action = getAction(deviceId, actionId)
            val scheduler = Scheduler(device.deviceId, deviceId, ScheduleType.Immediate, null, null, action)
            schedulerList.add(scheduler)
        }
    }


    fun actionTimeout(deviceId: String, actionId: String, timeout: Long) {

        val iterator = schedulerList.iterator()
        while (iterator.hasNext()) {
            val scheduler = iterator.next()
            if (scheduler.callerDeviceId == device.deviceId
                && scheduler.deviceId == deviceId
                && scheduler.action.id == actionId
                && scheduler.scheduleType == ScheduleType.Timeout
            ) {
                iterator.remove()
            }
        }

        val action = getAction(deviceId, actionId)
        val scheduler = Scheduler(device.deviceId, deviceId, ScheduleType.Timeout, timeout, null, action)
        schedulerList.add(scheduler)
    }

    fun actionFixedRate(deviceId: String, actionId: String, timeout: Long) {
        var found = false
        for (scheduler in schedulerList) {
            if (scheduler.callerDeviceId == device.deviceId
                && scheduler.deviceId == deviceId
                && scheduler.action.id == actionId
                && scheduler.scheduleType == ScheduleType.FixedRate
            ) {
                found = true
                break
            }
        }
        if (!found) {
            val action = getAction(deviceId, actionId)
            val scheduler = Scheduler(device.deviceId, deviceId, ScheduleType.FixedRate, timeout, null, action)
            schedulerList.add(scheduler)
        }
    }

    fun actionCron(deviceId: String, actionId: String, cronDefinition: String) {
        var found = false
        for (scheduler in schedulerList) {
            if (scheduler.callerDeviceId == device.deviceId
                && scheduler.deviceId == deviceId
                && scheduler.action.id == actionId
                && scheduler.cronDefinition == cronDefinition
                && scheduler.scheduleType == ScheduleType.CronScheduler
            ) {
                found = true
                break
            }
        }
        if (!found) {
            val action = getAction(deviceId, actionId)
            val scheduler =
                Scheduler(device.deviceId, deviceId, ScheduleType.CronScheduler, null, cronDefinition, action)
            schedulerList.add(scheduler)
        }
    }

}