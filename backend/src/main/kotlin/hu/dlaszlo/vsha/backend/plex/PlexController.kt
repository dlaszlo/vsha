package hu.dlaszlo.vsha.backend.plex

import hu.dlaszlo.vsha.backend.mqtt.MqttService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

@RestController
@RequestMapping("/plex")
class PlexController {

    @Autowired
    lateinit var mqttService: MqttService

    @PostMapping(value = ["/webhook"])
    @ResponseStatus(value = HttpStatus.OK)
    fun webhook(request: MultipartHttpServletRequest, @RequestParam("files") files: Array<MultipartFile?>) {
        val payload: String = request.getParameter("payload")
        logger.info("Plex event: {}", payload)
        mqttService.publish("plex/webhook", payload, false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(PlexController::class.java)!!
    }

}