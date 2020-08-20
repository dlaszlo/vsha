package hu.dlaszlo.vsha.backend.plex.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Event (
    val event: String? = null,
    @JsonProperty("Player") val player: Player? = null,
    @JsonProperty("Metadata") val metadata: Metadata? = null
)
