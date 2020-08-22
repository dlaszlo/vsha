package hu.dlaszlo.vsha.backend.device

interface Switch {

    val switchState: SwitchState

    fun getState(): Boolean

    fun powerOn(): Boolean

    fun powerOff(): Boolean

    fun toggle(): Boolean

}