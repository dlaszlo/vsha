package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component("kornyezetErzekeloKonyha")
class KornyezetErzekeloKonyha : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "ambs4",
        val name: String = "Környezet érzékelő - konyha"
    )

    var state = DeviceState()

    override var device = device {

        subscribe {
            topic = state.mqttName
            handler = { payload ->
                logger.info(payload)

                val pressure: Double = jsonValue(payload, "$.pressure", 0.0)!!
                val gasResistance: Double = jsonValue(payload, "$.gasResistance", 0.0)!!
                val iaq: Double = jsonValue(payload, "$.iaq", 0.0)!!
                val iaqAccuracy: Int = jsonValue(payload, "$.iaqAccuracy", 0)!!
                val temperature: Double = jsonValue(payload, "$.temperature", 0.0)!!
                val humidity: Double = jsonValue(payload, "$.humidity", 0.0)!!
                val dewPointTemperature = calculateDewPoint(humidity, temperature)
                val breathVocEquivalent: Double = jsonValue(payload, "$.breathVocEquivalent", 0.0)!!
                val breathVocAccuracy: Int = jsonValue(payload, "$.breathVocAccuracy", 0)!!
                val co2Equivalent: Double = jsonValue(payload, "$.co2Equivalent", 0.0)!!
                val co2Accuracy: Int = jsonValue(payload, "$.co2Accuracy", 0)!!
                val compGasValue: Double = jsonValue(payload, "$.compGasValue", 0.0)!!
                val compGasAccuracy: Int = jsonValue(payload, "$.compGasAccuracy", 0)!!
                val gasPercentage: Double = jsonValue(payload, "$.gasPercentage", 0.0)!!
                val gasPercentageAcccuracy: Int = jsonValue(payload, "$.gasPercentageAcccuracy", 0)!!
                val staticIaq: Double = jsonValue(payload, "$.staticIaq", 0.0)!!
                val staticIaqAccuracy: Int = jsonValue(payload, "$.staticIaqAccuracy", 0)!!
                val stabStatus: Int = jsonValue(payload, "$.stabStatus", 0)!!

                influxDB.write(
                    Point.measurement("ambs4")
                        .time(currentTime(), TimeUnit.MILLISECONDS)
                        .addField("pressure", pressure)
                        .addField("gasResistance", gasResistance)
                        .addField("iaq", iaq)
                        .addField("iaqAccuracy", iaqAccuracy)
                        .addField("temperature", temperature)
                        .addField("humidity", humidity)
                        .addField("dewPointTemperature", dewPointTemperature)
                        .addField("breathVocEquivalent", breathVocEquivalent)
                        .addField("breathVocAccuracy", breathVocAccuracy)
                        .addField("co2Equivalent", co2Equivalent)
                        .addField("co2Accuracy", co2Accuracy)
                        .addField("compGasValue", compGasValue)
                        .addField("compGasAccuracy", compGasAccuracy)
                        .addField("gasPercentage", gasPercentage)
                        .addField("gasPercentageAcccuracy", gasPercentageAcccuracy)
                        .addField("staticIaq", staticIaq)
                        .addField("staticIaqAccuracy", staticIaqAccuracy)
                        .addField("stabStatus", stabStatus)
                        .build()
                )
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(KornyezetErzekeloKonyha::class.java)!!
    }
}