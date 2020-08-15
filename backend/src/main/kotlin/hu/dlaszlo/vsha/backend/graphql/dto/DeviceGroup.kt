package hu.dlaszlo.vsha.backend.graphql.dto

data class DeviceGroup(
    var displayOrder: Int,
    val groupName: String,
    val devices: MutableList<DeviceInfo> = mutableListOf()
)