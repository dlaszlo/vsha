package hu.dlaszlo.vsha.device

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.TimeUnit

@Component
class SmsService {

    @Value("\${sms.to}")
    private lateinit var toPhoneNumber: String

    fun sendSms(text: String) {
        val toArr = toPhoneNumber.split(",").toTypedArray()
        for (t in toArr) {

            val commandParts = arrayOf(
                "/usr/bin/gammu-smsd-inject",
                "TEXT", t.trim(),
                "-text", text
            )

            val proc = ProcessBuilder(*commandParts)
                .directory(File(System.getProperty("user.dir")))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(10, TimeUnit.MINUTES)

            if (proc.errorStream.available() > 0) {
                val err = proc.errorStream.bufferedReader().readText()
                logger.warn("{}", err)
            }
            if (proc.inputStream.available() > 0) {
                val out = proc.inputStream.bufferedReader().readText()
                logger.info("{}", out)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(SmsService::class.java)!!
    }

}