package hu.dlaszlo.vsha.backend.plex

import com.fasterxml.jackson.databind.ObjectMapper
import hu.dlaszlo.vsha.backend.mqtt.MqttService
import hu.dlaszlo.vsha.backend.plex.model.Event
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

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @PostMapping(value = ["/webhook"])
    @ResponseStatus(value = HttpStatus.OK)
    fun webhook(request: MultipartHttpServletRequest, @RequestParam("files") files: Array<MultipartFile?>) {
        val payload: String = request.getParameter("payload")
        val event: Event? = objectMapper.readValue(payload, Event::class.java)
        logger.info("Plex event: {}", event)
        if (event != null) {
            val mqttMessage = objectMapper.writeValueAsString(event)
            mqttService.publish("plex/webhook", mqttMessage, false)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(PlexController::class.java)!!
    }

}