package hu.dlaszlo.vsha.device

abstract class SwitchState {
    abstract var name: String
    var online: Boolean = false
    var powerOn: Boolean = false
}
