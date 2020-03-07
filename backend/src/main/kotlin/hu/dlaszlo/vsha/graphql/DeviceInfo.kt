package hu.dlaszlo.vsha.graphql

data class DeviceInfo (
    val deviceId: String,
    val name: String,
    val online: Boolean,
    val powerOn: Boolean
)
