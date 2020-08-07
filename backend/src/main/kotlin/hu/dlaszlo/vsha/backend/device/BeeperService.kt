package hu.dlaszlo.vsha.backend.device

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Component
class BeeperService {

    @Value("\${beeper.enabled}")
    private var beeperEnabled: Boolean = false

    @Value("\${beeper.pin.number}")
    private var beeperPinNumber: String = "21"

    fun beep(vararg delaysInMillis: Long) {
        logger.info("Beep: {}", delaysInMillis.joinToString(", "))
        if (beeperEnabled) {
            var state = false
            for (delay in delaysInMillis) {
                if (state) {
                    File("/sys/class/gpio/gpio$beeperPinNumber/value").writeText("0")
                } else {
                    File("/sys/class/gpio/gpio$beeperPinNumber/value").writeText("1")
                }
                state = !state
                Thread.sleep(delay)
            }
            if (state) {
                File("/sys/class/gpio/gpio$beeperPinNumber/value").writeText("0")
            }
        }
    }

    @PostConstruct
    fun initialize() {
        if (beeperEnabled) {
            if (!File("/sys/class/gpio/gpio$beeperPinNumber").exists()) {
                File("/sys/class/gpio/export").writeText(beeperPinNumber)
                File("/sys/class/gpio/gpio$beeperPinNumber/direction").writeText("out")
            }
        }
    }

    @PreDestroy
    fun cleanUp() {
        if (beeperEnabled) {
            File("/sys/class/gpio/gpio$beeperPinNumber/value").writeText("0")
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(BeeperService::class.java)!!
    }

}