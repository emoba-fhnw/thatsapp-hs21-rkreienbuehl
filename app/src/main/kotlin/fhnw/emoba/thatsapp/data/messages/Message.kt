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
}