package hu.dlaszlo.vsha.device

class Action {
    var id: String? = null
    var allow: (callerDeviceId: String) -> Boolean = {true}
    var handler: (callerDeviceId: String) -> Unit = {}
}
