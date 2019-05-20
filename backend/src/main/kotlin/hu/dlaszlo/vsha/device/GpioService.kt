package hu.dlaszlo.vsha.device

import com.pi4j.io.gpio.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Component
class GpioService {

    @Value("\${beeper.enabled}")
    private var beeperEnabled: Boolean = false

    var gpio: GpioController? = null
    var beeper: GpioPinDigitalOutput? = null

    fun beep(vararg delaysInMillis: Long) {
        logger.info("Beep: {}", delaysInMillis.joinToString(", "))
        if (beeperEnabled) {
            var state = false
            for (delay in delaysInMillis) {
                if (state) {
                    beeper?.low()
                } else {
                    beeper?.high()
                }
                state = !state
                Thread.sleep(delay)
            }
            if (state) {
                beeper?.low()
            }
        }
    }

    @PostConstruct
    fun initialize() {
        if (beeperEnabled) {
            gpio = GpioFactory.getInstance()
            beeper = gpio!!.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Beeper", PinState.LOW)
            beeper!!.setShutdownOptions(true, PinState.LOW)
        }
    }

    @PreDestroy
    fun cleanUp() {
        if (beeperEnabled) {
            gpio?.shutdown()
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(GpioService::class.java)!!
    }

}