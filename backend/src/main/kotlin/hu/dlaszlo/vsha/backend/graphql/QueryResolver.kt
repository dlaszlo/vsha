package hu.dlaszlo.vsha.backend.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.graphql.dto.DeviceGroup
import hu.dlaszlo.vsha.backend.graphql.dto.DeviceInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class QueryResolver : GraphQLQueryResolver {

    @Autowired
    lateinit var context: ApplicationContext

    fun getState(deviceId: String): DeviceInfo {
        val device = context.getBean(deviceId, AbstractDeviceConfig::class.java)
        val deviceState = (device as Switch).switchState
        return DeviceInfo(
            deviceId = device.device.deviceId,
            displayOrder = deviceState.displayOrder,
            groupName = deviceState.mqttName,
            mqttName = deviceState.mqttName,
            name = deviceState.name,
            online = deviceState.online,
            powerOn = deviceState.powerOn
        )
    }

    fun getGroups(): List<DeviceGroup> {

        val map = LinkedHashMap<String, DeviceGroup>()

        val deviceMap = context.getBeansOfType(AbstractDeviceConfig::class.java)

        var devices = deviceMap.values
            .filter { it is Switch }
            .map { it as Switch }
            .sortedBy { it.switchState.displayOrder }

        for (device in devices) {
            val deviceInfo = DeviceInfo(
                deviceId = (device as AbstractDeviceConfig).device.deviceId,
                displayOrder = device.switchState.displayOrder,
                groupName = device.switchState.mqttName,
                mqttName = device.switchState.mqttName,
                name = device.switchState.name,
                online = device.switchState.online,
                powerOn = device.switchState.powerOn
            )
            val deviceGroups = map.getOrPut(device.switchState.groupName) {
                DeviceGroup(
                    displayOrder = device.switchState.displayOrder,
                    groupName = device.switchState.groupName
                )
            }
            deviceGroups.devices.add(deviceInfo)
        }

        return map.values.toList()
    }

}