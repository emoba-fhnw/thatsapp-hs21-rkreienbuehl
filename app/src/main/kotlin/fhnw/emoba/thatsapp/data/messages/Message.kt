package fhnw.emoba.thatsapp.data.messages

import java.util.*

abstract class Message(id: UUID, senderID: UUID, metaInfo: String) {
    var id = id
    var senderID = senderID
    abstract var type: String
    abstract var subtype: String
    var sendTime = Date()
    var metaInfo = metaInfo

    abstract fun asJSON(): String
}