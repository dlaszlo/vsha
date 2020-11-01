package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component("kornyezetErzekeloNappali")
class KornyezetErzekeloNappali : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "ambs2",
        val name: String = "Környezet érzékelő - nappali"
    )

    var state = DeviceState()

    override var device = device {

        subscribe {
            topic = state.mqttName
            handler = { payload ->
                logger.info(payload)

                val pressure: Double = jsonValue(payload, "$.pressure")!!
                val gasResistance: Double = jsonValue(payload, "$.gasResistance")!!
                val iaq: Double = jsonValue(payload, "$.iaq")!!
                val iaqAccuracy: Int = jsonValue(payload, "$.iaqAccuracy")!!
                val temperature: Double = jsonValue(payload, "$.temperature")!!
                val humidity: Double = jsonValue(payload, "$.humidity")!!
                val dewPointTemperature = calculateDewPoint(humidity, temperature)
                val breathVocEquivalent: Double = jsonValue(payload, "$.breathVocEquivalent")!!
                val breathVocAccuracy: Int = jsonValue(payload, "$.breathVocAccuracy")!!
                val co2Equivalent: Double = jsonValue(payload, "$.co2Equivalent")!!
                val co2Accuracy: Int = jsonValue(payload, "$.co2Accuracy")!!
                val compGasValue: Double = jsonValue(payload, "$.compGasValue")!!
                val compGasAccuracy: Int = jsonValue(payload, "$.compGasAccuracy")!!
                val gasPercentage: Double = jsonValue(payload, "$.gasPercentage")!!
                val gasPercentageAcccuracy: Int = jsonValue(payload, "$.gasPercentageAcccuracy")!!
                val staticIaq: Double = jsonValue(payload, "$.staticIaq")!!
                val staticIaqAccuracy: Int = jsonValue(payload, "$.staticIaqAccuracy")!!
                val stabStatus: Int = jsonValue(payload, "$.stabStatus")!!

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
        val logger = LoggerFactory.getLogger(KornyezetErzekeloNappali::class.java)!!
    }
}