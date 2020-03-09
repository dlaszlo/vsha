package hu.dlaszlo.vsha.graphql

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import hu.dlaszlo.vsha.device.Switch
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

}