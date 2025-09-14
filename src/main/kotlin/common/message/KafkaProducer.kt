package org.example.common.message

import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.common.logging.Logging
import org.slf4j.Logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

enum class Topics(
    val topic: String,
) {
    Transactions("transactions"),
}

@Component
class KafkaProducer(
    private val template: KafkaTemplate<String, Any>,
    private val log: Logger = Logging.getLogger(KafkaProducer::class.java)
) {
    fun sendMessage(topic: String, message: Any) {
        try {
            template.send(topic, message).get()
            log.info("메시지 발행 성공 - topic: $topic - time: ${LocalDateTime.now()}")
        } catch (ex: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_MESSAGE_SEND, topic)
        }
    }
}