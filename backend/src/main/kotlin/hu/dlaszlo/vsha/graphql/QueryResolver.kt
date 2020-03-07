package hu.dlaszlo.vsha.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import hu.dlaszlo.vsha.config.KapcsoloFolyoso
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class QueryResolver : GraphQLQueryResolver {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    fun getStateKapcsoloFolyoso(): DeviceInfo {
        val deviceState = applicationContext.getBean(KapcsoloFolyoso::class.java).state
        return DeviceInfo(
            deviceId = deviceState.mqttName,
            name = deviceState.name,
            online = deviceState.online,
            powerOn = deviceState.powerOn
        )
    }

}