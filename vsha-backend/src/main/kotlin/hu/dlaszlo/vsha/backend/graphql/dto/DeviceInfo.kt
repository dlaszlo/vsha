package hu.dlaszlo.vsha.backend.graphql.dto

data class DeviceInfo (
    val deviceId: String,
    val displayOrder: Int,
    val groupName: String,
    val mqttName: String,
    val name: String,
    val online: Boolean,
    val powerOn: Boolean
)