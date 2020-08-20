package hu.dlaszlo.vsha.backend.plex.model

data class Player(
    val local: Boolean? = null,
    val publicAddress: String? = null,
    val title: String? = null,
    val uuid: String? = null
)