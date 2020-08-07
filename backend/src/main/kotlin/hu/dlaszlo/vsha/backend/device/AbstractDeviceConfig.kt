package hu.dlaszlo.vsha.backend.device

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import hu.dlaszlo.vsha.backend.graphql.SubscriptionResolver
import hu.dlaszlo.vsha.backend.mqtt.MqttService
import hu.dlaszlo.vsha.backend.sunsetsunrise.SunsetSunriseService
import hu.dlaszlo.vsha.backend.telegram.TelegramService
import org.influxdb.InfluxDB
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import kotlin.math.ln

abstract class AbstractDeviceConfig {


    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired(required = false)
    lateinit var influxDB: InfluxDB

    @Autowired
    lateinit var mqttService: MqttService

    @Autowired
    lateinit var telegramService: TelegramService

    @Autowired
    lateinit var beeperService: BeeperService

    @Autowired
    lateinit var sunsetSunriseService: SunsetSunriseService

    @Autowired
    lateinit var subscriptions: SubscriptionResolver

    abstract var device: Device

    val schedulerList = mutableListOf<Scheduler<out AbstractDeviceConfig>>()

    protected fun publish(topic: String, payload: String, retained: Boolean) {
        mqttService.publish(topic, payload, retained)
    }

    fun device(dev: Device.() -> Unit): Device = Device().apply(dev)

    fun Device.initialize(init: () -> Unit) {
        this.initialize = init
    }

    fun Device.subscribe(subscribe: Subscribe.() -> Unit) {
        subscribeList.add(Subscribe().apply(subscribe))
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

    fun currentTime(): Long {
        return System.currentTimeMillis()
    }

    fun minutes(minutes: Long): Long {
        return TimeUnit.MINUTES.toMillis(minutes)
    }

    fun seconds(seconds: Long): Long {
        return TimeUnit.SECONDS.toMillis(seconds)
    }

    inline fun <reified T : AbstractDeviceConfig> getDevice(): T {
        return applicationContext.getBean(T::class.java)
    }

    inline fun <reified T : AbstractDeviceConfig> action(noinline action: (t: T) -> Boolean) {
        var found = false
        val device = applicationContext.getBean(T::class.java)
        for (scheduler in schedulerList) {
            if (scheduler.scheduleType == ScheduleType.Immediate
                && scheduler.device == device
                && scheduler.action == action
            ) {
                found = true
                break
            }
        }
        if (!found) {
            val scheduler = Scheduler(device, action, ScheduleType.Immediate, null, null)
            schedulerList.add(scheduler)
        }
    }


    inline fun <reified T : AbstractDeviceConfig> clearTimeout(noinline action: (t: T) -> Boolean) {
        val device = applicationContext.getBean(T::class.java)
        val iterator = schedulerList.iterator()
        while (iterator.hasNext()) {
            val scheduler = iterator.next()
            if (scheduler.scheduleType == ScheduleType.Timeout
                && scheduler.device == device
                && scheduler.action == action
            ) {
                iterator.remove()
            }
        }
    }

    inline fun <reified T : AbstractDeviceConfig> actionTimeout(noinline action: (t: T) -> Boolean, timeout: Long) {
        clearTimeout(action)
        val device = applicationContext.getBean(T::class.java)
        val scheduler = Scheduler(device, action, ScheduleType.Timeout, timeout, null)
        schedulerList.add(scheduler)
    }

    inline fun <reified T : AbstractDeviceConfig> actionFixedRate(noinline action: (t: T) -> Boolean, timeout: Long) {
        var found = false
        val device = applicationContext.getBean(T::class.java)
        for (scheduler in schedulerList) {
            if (scheduler.scheduleType == ScheduleType.FixedRate
                && scheduler.device == device
                && scheduler.action == action
            ) {
                found = true
                break
            }
        }
        if (!found) {
            val scheduler = Scheduler(device, action, ScheduleType.FixedRate, timeout, null)
            schedulerList.add(scheduler)
        }
    }

    inline fun <reified T : AbstractDeviceConfig> actionCron(
        noinline action: (t: T) -> Boolean,
        cronDefinition: String
    ) {
        var found = false
        val device = applicationContext.getBean(T::class.java)
        for (scheduler in schedulerList) {
            if (scheduler.scheduleType == ScheduleType.CronScheduler
                && scheduler.device == device
                && scheduler.cronDefinition == cronDefinition
                && scheduler.action == action
            ) {
                found = true
                break
            }
        }
        if (!found) {
            val scheduler = Scheduler(device, action, ScheduleType.CronScheduler, null, cronDefinition)
            schedulerList.add(scheduler)
        }
    }

}