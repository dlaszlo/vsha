package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component("kornyezetErzekeloGyerekszoba")
class KornyezetErzekeloGyerekszoba : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "ambs2",
        val name: String = "Környezet érzékelő gyerekszoba ($mqttName)"
    )

    var state = DeviceState()


    override var device = device {

        subscribe {
            topic = "${state.mqttName}"
            handler = { payload ->
                logger.info(payload)

                val pressure: Double = jsonValue(payload, "$.pressure")!!
                val gasResistance: Double = jsonValue(payload, "$.gasResistance")!!
                val iaq: Double = jsonValue(payload, "$.iaq")!!
                val iaqAccuracy: Int = jsonValue(payload, "$.iaqAccuracy")!!
                val temperature: Double = jsonValue(payload, "$.temperature")!!
                val humidity: Double = jsonValue(payload, "$.humidity")!!
                val dewPointTemperature = calculateDewPoint(humidity, temperature)

                logger.info(
                    "Légköri nyomás: {}, Gáz ellenállás: {}, Levegő minőség: {}, Levegő minőség pontossága: {}, Hőmérséklet: {}, Relatív páratartalom: {}, Harmatponti hőmérséklet: {}",
                    pressure, gasResistance, iaq, iaqAccuracy, temperature, humidity, dewPointTemperature
                )

                influxDB.write(
                    Point.measurement("ambs2")
                        .time(currentTime(), TimeUnit.MILLISECONDS)
                        .addField("pressure", pressure)
                        .addField("gasResistance", gasResistance)
                        .addField("iaq", iaq)
                        .addField("iaqAccuracy", iaqAccuracy)
                        .addField("temperature", temperature)
                        .addField("humidity", humidity)
                        .addField("dewPointTemperature", dewPointTemperature)
                        .build()
                )
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(KornyezetErzekeloGyerekszoba::class.java)!!
    }
}