package hu.dlaszlo.vsha.device

import io.github.rybalkinsd.kohttp.dsl.httpGet
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

class SmsService {

    @Value("\${sms.api.host}")
    private lateinit var hostParam: String

    @Value("\${sms.api.path}")
    private lateinit var pathParam: String

    @Value("\${sms.api.username}")
    private lateinit var username: String

    @Value("\${sms.api.password}")
    private lateinit var password: String

    @Value("\${sms.api.from}")
    private lateinit var from: String

    @Value("\${sms.api.to}")
    private lateinit var to: String

    fun sendSms(text: String) {
        val toArr = to.split(",").toTypedArray()
        for (t in toArr) {
            val response: Response = httpGet {
                host = hostParam
                path = pathParam
                param {
                    "username" to username
                    "password" to password
                    "from" to from
                    "to" to t
                    "text" to text
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(SmsService::class.java)!!
    }

}