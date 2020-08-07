package hu.dlaszlo.vsha.backend.device

class Device {
    lateinit var deviceId: String
    var initialize: () -> Unit = {}
    val subscribeList = mutableListOf<Subscribe>()
}
