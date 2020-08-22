package hu.dlaszlo.vsha.plex.model

data class Metadata(
    val studio: String?,
    val type: String?,
    val title: String?,
    val originalTitle: String?,
    val summary: String?,
    val contentRating: String?,
    val rating: Double?,
    val audienceRating: Double?,
    val year: Int?,
    val duration: Long?
)
