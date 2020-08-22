package hu.dlaszlo.vsha.mqtt.model

data class Message(
    var topic: String,
    var payload: String,
    var isRetained: Boolean
)
