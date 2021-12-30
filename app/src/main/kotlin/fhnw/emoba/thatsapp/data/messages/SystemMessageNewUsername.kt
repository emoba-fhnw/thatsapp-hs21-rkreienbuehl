package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*

class SystemMessageNewUsername(id: UUID, senderID: UUID, username: String, date: LocalDateTime, metaInfo: String) : Message(id, senderID, date, metaInfo) {
    override var type = "system"
    override var subtype = "newUsername"
    var data = ConnectData(username)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getString("username"),
                LocalDateTime.parse(obj.getString("sendTime")),
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )
    constructor(id: UUID, senderID: UUID, username: String, metaInfo: String) :
            this(id, senderID, username, LocalDateTime.now(), metaInfo)

    override fun asJSON(): String {
        return """
            {
                "id": "$id",
                "senderID": "$senderID",
                "type": "$type",
                "subtype": "$subtype",
                "data": {
                    "username": "${data.username}"
                },
                "sendTime": "$sendTime",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class ConnectData(username: String) {
        var username = username
    }
}