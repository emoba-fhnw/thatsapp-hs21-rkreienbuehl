package fhnw.emoba.thatsapp.data.messages

import fhnw.emoba.thatsapp.data.dateFromJSON
import fhnw.emoba.thatsapp.data.toJSONDateString
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SystemMessageNewChat(id: UUID, senderID: UUID, chatID: UUID, chatImageLink: String, members: List<String>, date: LocalDateTime, metaInfo: String) : Message(id, senderID, date, metaInfo) {
    override var type = "system"
    override var subtype = "newChat"
    var data = NewChatData(chatID, chatImageLink, members)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                UUID.fromString(obj.getJSONObject("data").getString("chatID")),
                obj.getJSONObject("data").getString("chatImageLink"),
                obj.getJSONObject("data").getJSONArray("members").parseMembers(),
                obj.getString("sendTime"),
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )
    constructor(id: UUID, senderID: UUID, chatID: UUID, chatImageLink: String, members: List<String>, date: String, metaInfo: String) :
            this(id, senderID, chatID, chatImageLink, members, date.dateFromJSON(), metaInfo)
    constructor(senderID: UUID, chatImageLink: String, members: List<String>, metaInfo: String) :
            this(UUID.randomUUID(), senderID, UUID.randomUUID(), chatImageLink, members, LocalDateTime.now(), metaInfo)

    override fun asJSON(): String {
        return """
            {
                "id": "$id",
                "senderID": "$senderID",
                "type": "$type",
                "subtype": "$subtype",
                "data": {
                    "chatID": "${data.chatID}",
                    "chatImageLink": "${data.chatImageLink}",
                    "members": ${data.members.toJSONArray()}
                },
                "sendTime": "${sendTime.toJSONDateString()}",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class NewChatData(chatID: UUID, chatImageLink: String, members: List<String>) {
        var chatID = chatID
        var chatImageLink = chatImageLink
        var members = members
    }
}

private fun List<String>.toJSONArray() : String {
    return this.joinToString("\", \"", "[\"", "\"]")
}

private fun JSONArray.parseMembers() : List<String> {
    val list: MutableList<String> = mutableListOf()
    for (i in 0 until this.length()) {
        list.add(this.getString(i))
    }
    return list
}