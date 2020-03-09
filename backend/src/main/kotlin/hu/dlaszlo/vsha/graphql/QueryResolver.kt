package hu.dlaszlo.vsha.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import hu.dlaszlo.vsha.config.KapcsoloFolyoso
import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Switch
import hu.dlaszlo.vsha.device.SwitchState
import hu.dlaszlo.vsha.graphql.dto.DeviceInfo
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