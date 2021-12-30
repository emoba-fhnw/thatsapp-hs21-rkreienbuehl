package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class SystemMessageNewProfileImage(id: UUID, senderID: UUID, profileImageLink: String, metaInfo: String) : Message(id, senderID, metaInfo) {
    override var type = "system"
    override var subtype = "newProfileImage"
    var data = NewProfileImageData(profileImageLink)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
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
                    "profileImageLink": "${data.profileImageLink}"
                },
                "sendTime": "$sendTime",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class NewProfileImageData(profileImageLink: String) {
        var profileImageLink = profileImageLink
    }
}