package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.backend.BackendConfiguration
import hu.dlaszlo.vsha.backend.service.HomeAutomationService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(BackendConfiguration::class)
class HomeAutomation

val logger = LoggerFactory.getLogger(HomeAutomation::class.java)!!

fun main(args: Array<String>) {
    val context = SpringApplicationBuilder(HomeAutomation::class.java).run(*args)
    context.getBean(HomeAutomationService::class.java).run()
}

