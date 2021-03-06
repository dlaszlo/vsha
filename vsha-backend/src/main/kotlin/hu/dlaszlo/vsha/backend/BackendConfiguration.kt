package hu.dlaszlo.vsha.backend

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import graphql.kickstart.servlet.apollo.ApolloScalars
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import hu.dlaszlo.vsha.mqtt.MqttConfiguration
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan
@Import(MqttConfiguration::class)
class BackendConfiguration {

    @Bean
    fun upload(): GraphQLScalarType {
        return ApolloScalars.Upload
    }

    @Bean
    fun date(): GraphQLScalarType {
        return ExtendedScalars.Date
    }

    @Bean
    fun dateTime(): GraphQLScalarType {
        return ExtendedScalars.DateTime
    }

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
        val logger = LoggerFactory.getLogger(BackendConfiguration::class.java)!!
    }

}