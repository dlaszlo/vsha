package hu.dlaszlo.vsha

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

}