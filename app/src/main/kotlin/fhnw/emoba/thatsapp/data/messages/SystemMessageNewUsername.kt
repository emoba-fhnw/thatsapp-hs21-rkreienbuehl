package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class SystemMessageNewUsername(id: UUID, senderID: UUID, username: String, metaInfo: String) : Message(id, senderID, metaInfo) {
    override var type = "system"
    override var subtype = "newUsername"
    var data = ConnectData(username)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getString("username"),
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