package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component("homero1")
open class Homero1 : AbstractDeviceConfig() {

    val calibrateRoomTemperature = -1.3

    override var device = device {

        mqttName = "homero1"
        name = "Konyhai hőmérő ($mqttName)"

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

                logger.info("Fal hőmérséklet: {}, Konyha hőmérséklet: {}, Légköri nyomás: {}, Becsült magasság: {}, Relatív páratartalom: {}, Harmatponti hőmérséklet: {}",
                    wallTemperature, roomTemperature, pressure, estimatedAltitude, relativeHumidity, dewPointTemperature)

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
}