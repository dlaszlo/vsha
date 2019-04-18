package hu.dlaszlo.vsha.device

import org.slf4j.Logger

class Device {
    lateinit var logger: Logger
    var mqttName: String? = null
    var name: String? = null
    var initialize: (() -> Unit)? = null
    val routeList = mutableListOf<Route>()
    val actionList = mutableListOf<Action>()
}
