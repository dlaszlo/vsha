package hu.dlaszlo.vsha.backend.sunsetsunrise

enum class Zenith(val degrees: Double) {
    ASTRONOMICAL(108.0),
    NAUTICAL(102.0),
    CIVIL(96.0),
    OFFICIAL(90.8333)
}