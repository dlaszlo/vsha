package hu.dlaszlo.vsha.mqtt

data class Message(
    var topic: String,
    var payload: String,
    var isRetained: Boolean
)
