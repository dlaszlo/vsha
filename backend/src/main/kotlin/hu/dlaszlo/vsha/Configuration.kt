package hu.dlaszlo.vsha

import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import graphql.servlet.apollo.ApolloScalars
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Configuration {

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

}