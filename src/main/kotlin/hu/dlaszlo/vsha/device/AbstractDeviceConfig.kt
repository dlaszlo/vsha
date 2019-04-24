package hu.dlaszlo.vsha.device

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import hu.dlaszlo.vsha.mqtt.Mqtt
import org.influxdb.InfluxDB
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import java.lang.Exception
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.ln

abstract class AbstractDeviceConfig {

    @Autowired
    private lateinit var mqtt: Mqtt

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired(required = false)
    lateinit var influxDB: InfluxDB

    abstract var device: Device

    val schedulerList = mutableListOf<Scheduler>()

    protected fun publish(topic: String, payload: String, retained: Boolean) {
        mqtt.publish(topic, payload, retained)
    }

    fun device(dev: Device.() -> Unit): Device = Device().apply(dev)

    fun Device.initialize(init: () -> Unit) {
        this.initialize = init
    }

    fun Device.subscribe(subscribe: Subscribe.() -> Unit) {
        subscribeList.add(Subscribe().apply(subscribe))
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

    inline fun <reified T> jsonValue(payload: String, jsonPath: String): T? {
        var pl: String? = null
        try {
            pl = (JsonPath.parse(payload).read(jsonPath) as Any).toString()
        } catch (e: PathNotFoundException) {
            //
        }
        return when (T::class) {
            String::class -> pl as T
            Byte::class -> pl?.toByte() as T
            Int::class -> pl?.toInt() as T
            Long::class -> pl?.toLong() as T
            Float::class -> pl?.toFloat() as T
            Double::class -> pl?.toDouble() as T
            BigDecimal::class -> pl?.toBigDecimal() as T
            BigInteger::class -> pl?.toBigInteger() as T
            Boolean::class -> pl?.toBoolean() as T
            else -> throw Exception("Unhandled return type")
        }
    }

    fun calculateDewPoint(relativeHumidity: Double, temperature: Double): Double {
        val m = 17.62
        val tn = 243.12
        val t1 = ln(relativeHumidity / 100)
        val t2 = (m * temperature) / (tn + temperature)
        return tn * (t1 + t2) / (m - (t1 + t2))
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