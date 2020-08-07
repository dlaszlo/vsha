package hu.dlaszlo.vsha.backend.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.graphql.dto.DeviceInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class QueryResolver : GraphQLQueryResolver {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    fun getState(deviceId: String): DeviceInfo {
        val device = applicationContext.getBean(deviceId, AbstractDeviceConfig::class.java)
        val deviceState = (device as Switch).switchState
        return DeviceInfo(
            deviceId = device.device.deviceId,
            name = deviceState.name,
            online = deviceState.online,
            powerOn = deviceState.powerOn
        )
    }

}