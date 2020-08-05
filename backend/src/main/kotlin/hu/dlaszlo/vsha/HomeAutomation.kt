package hu.dlaszlo.vsha

import org.slf4j.LoggerFactory
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder


@SpringBootApplication
class HomeAutomation

val logger = LoggerFactory.getLogger(HomeAutomation::class.java)!!

fun main(args: Array<String>) {

    val context = SpringApplicationBuilder(HomeAutomation::class.java)
        .web(WebApplicationType.NONE)
        .run(*args)

    context.getBean(HomeAutomationService::class.java).run()

}

