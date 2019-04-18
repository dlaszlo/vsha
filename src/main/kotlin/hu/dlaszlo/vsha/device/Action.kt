package hu.dlaszlo.vsha.device

class Action {
    var id: String? = null
    var handler: (() -> Unit)? = null
}
