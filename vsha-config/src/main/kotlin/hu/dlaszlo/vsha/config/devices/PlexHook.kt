package hu.dlaszlo.vsha.config.devices

import com.fasterxml.jackson.core.JsonParseException
import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Device
import hu.dlaszlo.vsha.plex.model.Event
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("plexHook")
class PlexHook : AbstractDeviceConfig() {

    override var device: Device = device {

        subscribe {
            topic = "plex/webhook"
            handler = {
                var event: Event? = null
                try {
                    event = objectMapper.readValue(it, Event::class.java)
                } catch (e: JsonParseException) {
                    logger.error(e.message, e)
                }
                if (event != null) {
                    when (event.event) {
                        "media.play" -> {
                            logger.info("Plex: A lejátszás indul.")
                            action(KapcsoloNappali::powerOff)
                            action(KonnektorIroasztalLampa::powerOff)
                            action(KonnektorNappali::powerOff)
                            sendMessage("A lejátszás elindult.", event, true)
                        }
                        "media.resume" -> {
                            logger.info("Plex: A lejátszás folytatódik.")
                            action(KapcsoloNappali::powerOff)
                            action(KonnektorIroasztalLampa::powerOff)
                            action(KonnektorNappali::powerOff)
                        }
                        "media.pause" -> {
                            logger.info("Plex: A lejátszás szünetel.")
                            action(KapcsoloNappali::powerOff)
                            action(KonnektorIroasztalLampa::powerOff)
                            action(KonnektorNappali::powerOn)
                        }
                        "media.stop" -> {
                            logger.info("Plex: A lejátszás végetért.")
                            action(KapcsoloNappali::powerOff)
                            action(KonnektorIroasztalLampa::powerOff)
                            action(KonnektorNappali::powerOn)
                            sendMessage("A lejátszás végetért.", event, false)
                        }
                        "media.scrobble" -> {
                            logger.info("Plex: A lejátszás hamarosan végetér.")
                            sendMessage("A lejátszás hamarosan végetér.", event, false)
                        }
                        else -> {
                            logger.info("Ismeretlen esemény típus: $event")
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage(message: String, event: Event, detailed: Boolean) {
        val sb: StringBuilder = StringBuilder(message).append("\n")
        if (event.metadata != null) {
            if (event.metadata!!.title != null) {
                sb.append("\n").append(event.metadata!!.title)
                if (event.metadata!!.originalTitle != null) {
                    sb.append(" (").append(event.metadata!!.originalTitle).append(")")
                }
            } else if (event.metadata!!.originalTitle != null) {
                sb.append("\n").append(event.metadata!!.originalTitle)
            }
            if (detailed) {
                if (event.metadata!!.studio != null) {
                    sb.append("\nStúdió: ").append(event.metadata!!.studio)
                }
                if (event.metadata!!.type != null) {
                    sb.append("\nTípus: ").append(event.metadata!!.type)
                }
                if (event.metadata!!.contentRating != null) {
                    sb.append("\nBesorolás: ").append(event.metadata!!.contentRating)
                }
                if (event.metadata!!.rating != null) {
                    sb.append("\nÉrtékelés: ").append(event.metadata!!.rating)
                }
                if (event.metadata!!.audienceRating != null) {
                    sb.append("\nKözönség értékelés: ").append(event.metadata!!.audienceRating)
                }
                if (event.metadata!!.year != null) {
                    sb.append("\nÉv: ").append(event.metadata!!.year)
                }
                if (event.metadata!!.duration != null) {
                    val duration = event.metadata!!.duration?.div(60000)
                    sb.append("\nHossz: ").append(duration).append(" perc")
                }
                if (event.metadata!!.summary != null) {
                    sb.append("\n\n").append(event.metadata!!.summary)
                }
            }
        }
        telegramService.sendMessage(sb.toString())
    }

    companion object {
        val logger = LoggerFactory.getLogger(PlexHook::class.java)!!
    }
}