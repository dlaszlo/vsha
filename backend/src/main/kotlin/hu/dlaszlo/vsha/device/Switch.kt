package hu.dlaszlo.vsha.device

interface Switch {

    fun powerOn(): Boolean

    fun powerOff(): Boolean

    fun toggle(): Boolean

}