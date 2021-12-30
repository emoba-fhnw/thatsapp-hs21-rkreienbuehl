package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class SystemMessageConnect(id: UUID, senderID: UUID, username: String, profileImageLink: String, metaInfo: String) : Message(id, senderID, metaInfo) {
    override var type = "system"
    override var subtype = "connect"
    var data = ConnectData(username, profileImageLink)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getString("username"),
                obj.getJSONObject("data").getString("profileImageLink"),
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
                    "username": ${data.username},
                    "profileImageLink": ${data.profileImageLink}
                },
                "sendTime": "$sendTime",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class ConnectData(username: String, profileImageLink: String) {
        var username = username
        var profileImageLink = profileImageLink
    }
}