package hu.dlaszlo.vsha.device

class Device {
    lateinit var deviceId: String
    var initialize: () -> Unit = {}
    val subscribeList = mutableListOf<Subscribe>()
}
