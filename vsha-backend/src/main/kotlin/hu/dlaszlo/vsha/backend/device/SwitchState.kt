package hu.dlaszlo.vsha.backend.device

abstract class SwitchState {
    abstract var displayOrder: Int
    abstract var groupName: String
    abstract var mqttName: String
    abstract var name: String
    var online: Boolean = false
    var powerOn: Boolean = false
}
