package fhnw.emoba.thatsapp.data.messages

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class SystemMessageNewChat(id: UUID, senderID: UUID, chatID: UUID, chatImageLink: String, members: List<String>, metaInfo: String) : Message(id, senderID, metaInfo) {
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
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )

    override fun asJSON(): String {
        return """
            {
                "id": "$id",
                "senderID": "$senderID",
                "type": "$type",
                "subtype": "$subtype",
                "data": {
                    "chatID": ${data.chatID},
                    "chatImageLink": ${data.chatImageLink},
                    "members": ${data.members}
                },
                "sendTime": "$sendTime",
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

private fun JSONArray.parseMembers() : List<String> {
    val list: MutableList<String> = mutableListOf()
    for (i in 0 until this.length()) {
        list.add(this.getString(i))
    }
    return list
}