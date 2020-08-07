package hu.dlaszlo.vsha.backend.graphql.dto

class DeviceInfo (
    val deviceId: String,
    val name: String,
    val online: Boolean,
    val powerOn: Boolean
)