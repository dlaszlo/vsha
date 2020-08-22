package hu.dlaszlo.vsha.backend.mqtt

data class Message(
    var topic: String,
    var payload: String,
    var isRetained: Boolean
)
