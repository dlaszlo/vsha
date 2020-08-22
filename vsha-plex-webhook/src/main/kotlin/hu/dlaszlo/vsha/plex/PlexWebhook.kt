package hu.dlaszlo.vsha.plex

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class PlexWebhook

val logger = LoggerFactory.getLogger(PlexWebhook::class.java)!!

fun main(args: Array<String>) {
    runApplication<PlexWebhook>(*args)
}

