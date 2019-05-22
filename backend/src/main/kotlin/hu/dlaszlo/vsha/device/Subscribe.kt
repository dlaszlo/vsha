package hu.dlaszlo.vsha.device

class Subscribe {
    var topic: String? = null
    var topicList: List<String>? = null
    var payload: String? = null
    var payloadList: List<String>? = null
    var jsonPath: String? = null
    var handler: (payload: String) -> Unit = {}
}