package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class HomeroKonyha : AbstractDeviceConfig() {

    final val calibrateRoomTemperature = -1.3
    final val mqttName = "homero1"
    final val name = "Konyhai hőmérő ($mqttName)"

    override var device = device {

        subscribe {
            topic = "$mqttName/adat"
            handler = { payload ->
                logger.info(payload)

                val wallTemperature: Double = jsonValue(payload, "$.t1")!!
                var roomTemperature: Double = jsonValue(payload, "$.t2")!!
                roomTemperature += calibrateRoomTemperature
                val pressure: Double = jsonValue(payload, "$.p")!!
                val estimatedAltitude: Double = jsonValue(payload, "$.a")!!
                val relativeHumidity: Double = jsonValue(payload, "$.h")!!
                val dewPointTemperature = calculateDewPoint(relativeHumidity, roomTemperature)

                logger.info(
                    "Fal hőmérséklet: {}, Konyha hőmérséklet: {}, Légköri nyomás: {}, Becsült magasság: {}, Relatív páratartalom: {}, Harmatponti hőmérséklet: {}",
                    wallTemperature, roomTemperature, pressure, estimatedAltitude, relativeHumidity, dewPointTemperature
                )

                influxDB.write(
                    Point.measurement("homero1")
                        .time(currentTime(), TimeUnit.MILLISECONDS)
                        .addField("wallTemperature", wallTemperature)
                        .addField("roomTemperature", roomTemperature)
                        .addField("pressure", pressure)
                        .addField("estimatedAltitude", estimatedAltitude)
                        .addField("relativeHumidity", relativeHumidity)
                        .addField("dewPointTemperature", dewPointTemperature)
                        .build()
                )
            }
        }

    }

    companion object {
        val logger = LoggerFactory.getLogger(HomeroKonyha::class.java)!!
    }


}