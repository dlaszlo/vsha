package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.hateoas.Resource
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("homeroKonyha")
class HomeroKonyha : AbstractDeviceConfig() {

    class DeviceState : ResourceSupport() {
        val mqttName: String = "homero1"
        val name: String = "Konyhai hőmérő ($mqttName)"
    }

    var state = DeviceState()

    val calibrateRoomTemperature = -1.3

    override var device = device {

        subscribe {
            topic = "${state.mqttName}/adat"
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

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val link1 = linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel()
        return Resource(state, link1)
    }

    companion object {
        val logger = LoggerFactory.getLogger(HomeroKonyha::class.java)!!
    }


}