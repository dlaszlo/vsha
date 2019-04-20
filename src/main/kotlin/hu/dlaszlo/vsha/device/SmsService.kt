package hu.dlaszlo.vsha.device

import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.ext.url
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SmsService {

    @Value("\${sms.api.url}")
    private lateinit var urlParam: String

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
                url(urlParam)
                param {
                    "username" to username
                    "password" to password
                    "from" to from
                    "to" to t
                    "text" to text
                }
            }
            if (response.isSuccessful) {
                logger.info(response.message())
                logger.info(response.toString())
                logger.info(response.body()?.string())
            }
            else
            {
                logger.error(response.message())
                logger.error(response.toString())
                logger.error(response.body()?.string())
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(SmsService::class.java)!!
    }

}