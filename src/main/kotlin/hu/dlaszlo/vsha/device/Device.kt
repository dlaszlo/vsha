package hu.dlaszlo.vsha.device

import org.slf4j.Logger
import java.util.concurrent.TimeUnit

class Device {
    lateinit var logger: Logger
    lateinit var deviceId: String
    var mqttName: String? = null
    var name: String? = null
    var initialize: () -> Unit = {}
    val subscribeList = mutableListOf<Subscribe>()
    val actionList = mutableListOf<Action>()

    fun currentTime(): Long {
        return System.currentTimeMillis()
    }

    fun minutes(minutes: Long): Long {
        return TimeUnit.MINUTES.toMillis(minutes)
    }

    fun seconds(seconds: Long): Long {
        return TimeUnit.SECONDS.toMillis(seconds)
    }

}
