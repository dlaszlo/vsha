package hu.dlaszlo.vsha.backend.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TelegramService {

    @Value("\${telegram.bot_token}")
    private lateinit var botToken: String

    @Value("\${telegram.chat_id}")
    private var chatId: Long = 0

    var bot: TelegramBot? = null

    private fun sendMessage(message: String, notification: Boolean) {
        logger.info("Üzenet küldése: \"{}\", notification: {}", message, notification)
        val response = bot!!.execute(
            SendMessage(chatId, message)
                .disableNotification(!notification)
        )
        if (response.isOk) {
            logger.info("Az üzenet küldése megtörtént")
        } else {
            logger.error(
                "Az üzenet küldése sikertelen: {}, hibakód: {}",
                response.description(),
                response.errorCode()
            )
        }
    }

    fun sendMessage(message: String) {
        sendMessage(message, false)
    }

    fun sendNotification(message: String) {
        sendMessage(message, true)
    }

    @PostConstruct
    fun init() {
        bot = TelegramBot(botToken)
    }

    companion object {
        val logger = LoggerFactory.getLogger(TelegramService::class.java)!!
    }

}