package hu.dlaszlo.vsha.plex

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import hu.dlaszlo.vsha.mqtt.MqttConfiguration
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan
@Import(MqttConfiguration::class)
class PlexWebhookConfiguration {

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModule(Jdk8Module())
        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }

    companion object {
        val logger = LoggerFactory.getLogger(PlexWebhookConfiguration::class.java)!!
    }

}