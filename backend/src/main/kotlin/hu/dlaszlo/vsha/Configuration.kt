package hu.dlaszlo.vsha

import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import graphql.servlet.apollo.ApolloScalars
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor


@Configuration
class Configuration {

    @Bean("main_executor")
    fun taskExecutor() : TaskExecutor  {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 4
        executor.maxPoolSize = 4
        executor.threadNamePrefix = "main_executor"
        executor.initialize()
        return executor
    }

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