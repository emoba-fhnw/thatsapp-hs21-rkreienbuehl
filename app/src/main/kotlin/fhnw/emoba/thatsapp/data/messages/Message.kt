package fhnw.emoba.thatsapp.data.messages

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

abstract class Message(id: UUID, senderID: UUID, sendTime: LocalDateTime, metaInfo: String) {
    var id = id
    var senderID = senderID
    abstract var type: String
    abstract var subtype: String
    var sendTime = sendTime
    var metaInfo = metaInfo

    constructor(id: UUID, senderID: UUID, metaInfo: String) : this(id, senderID, LocalDateTime.now(), metaInfo)

    abstract fun asJSON(): String

    fun getFormattedDateString(): String = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.GERMAN).format(sendTime)

    companion object {
        fun parseDateString(date: String) : LocalDateTime =
            LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.GERMAN))
    }
}