package hu.dlaszlo.vsha.device

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.TimeUnit

@Component
class SmsService {

    @Value("\${sms.working.dir}")
    private lateinit var workingDir: String

    @Value("\${sms.command}")
    private lateinit var smsCommand: String

    @Value("\${sms.to}")
    private lateinit var toPhoneNumber: String

    fun sendSms(text: String) {
        val toArr = toPhoneNumber.split(",").toTypedArray()
        for (t in toArr) {
            val commandParts = smsCommand
                .replace("%%PHONE_NUMBER%%", t)
                .replace("%%TEXT%%", text)
                .split("\\s".toRegex())

            val proc = ProcessBuilder(*commandParts.toTypedArray())
                .directory(File(workingDir))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(10, TimeUnit.MINUTES)

            if (proc.errorStream.available() > 0) {
                val err = proc.errorStream.bufferedReader().readText()
                logger.error("{}", err)
            }
            if (proc.inputStream.available() > 0) {
                val out = proc.inputStream.bufferedReader().readText()
                logger.error("{}", out)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(SmsService::class.java)!!
    }

}