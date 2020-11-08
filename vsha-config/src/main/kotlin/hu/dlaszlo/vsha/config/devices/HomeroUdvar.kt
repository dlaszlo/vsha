package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component("homeroUdvar")
class HomeroUdvar : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "tempsens1",
        val name: String = "Hőmérő udvar"
    )

    var state = DeviceState()

    override var device = device {

        subscribe {
            topic = state.mqttName
            handler = { payload ->
                logger.info(payload)

                val pressure: Double = jsonValue(payload, "$.pressure", 0.0)!!
                val temperature: Double = jsonValue(payload, "$.temperature", 0.0)!!
                val humidity: Double = jsonValue(payload, "$.humidity", 0.0)!!
                val altitude: Double = jsonValue(payload, "$.altitude", 0.0)!!

                influxDB.write(
                    Point.measurement("tempsens1")
                        .time(currentTime(), TimeUnit.MILLISECONDS)
                        .addField("pressure", pressure)
                        .addField("temperature", temperature)
                        .addField("humidity", humidity)
                        .addField("altitude", altitude)
                        .build()
                )
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(HomeroUdvar::class.java)!!
    }
}