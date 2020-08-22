package hu.dlaszlo.vsha.backend.graphql

import graphql.kickstart.tools.GraphQLMutationResolver
import hu.dlaszlo.vsha.backend.device.Switch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class MutationResolver : GraphQLMutationResolver {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    fun updateState(deviceId: String): Boolean {
        return applicationContext.getBean(deviceId, Switch::class.java).getState()
    }

    fun toggle(deviceId: String): Boolean {
        return applicationContext.getBean(deviceId, Switch::class.java).toggle()
    }

    fun power(deviceId: String, powerOn: Boolean): Boolean {
        val switch = applicationContext.getBean(deviceId, Switch::class.java)
        return if (powerOn) switch.powerOn() else switch.powerOff()
    }

}