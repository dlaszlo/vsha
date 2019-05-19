package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.sms.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Resource
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Arrays.asList

@RestController
@RequestMapping("erzekeloVizelfolyasMosogep")
class ErzekeloVizelfolyasMosogep : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    class DeviceState : ResourceSupport() {
        val mqttName1: String = "konyha-rfbridge"
        val mqttName2: String = "nappali-rfbridge"
        val name: String = "Vízelfolyás érzékelő - mosógép ($mqttName1)"
    }

    var state = DeviceState()

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "540412"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Riasztás! Vízelfolyás érzékelő (mosógép).")
                action(ErzekeloVizelfolyasMosogep::sendSms)
            }
        }
    }

    fun sendSms(): Boolean {
        if (currentTime() - lastSms > minutes(1)) {
            logger.info("SMS küldése")
            lastSms = currentTime()
            smsService.sendSms("Riasztás! Vízelfolyás érzékelő (mosógép).")
        }
        return true
    }

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val link1 = linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel()
        return Resource(state, link1)
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloVizelfolyasMosogep::class.java)!!
    }


}
