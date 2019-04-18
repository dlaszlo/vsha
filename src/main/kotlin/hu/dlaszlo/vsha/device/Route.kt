package hu.dlaszlo.vsha.device

class Route {
    var topic: String? = null
    var payload: String? = null
    var jsonPath: String? = null
    var handler: ((payload: String) -> Unit)? = null
}