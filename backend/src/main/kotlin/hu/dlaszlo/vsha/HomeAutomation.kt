package hu.dlaszlo.vsha

import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class HomeAutomation

val logger = LoggerFactory.getLogger(HomeAutomation::class.java)!!

fun main(args: Array<String>) {
    runApplication<HomeAutomation>(*args)
}

