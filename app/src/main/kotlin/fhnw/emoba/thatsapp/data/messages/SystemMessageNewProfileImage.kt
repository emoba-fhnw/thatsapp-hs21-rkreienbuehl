package fhnw.emoba.thatsapp.data.messages

import fhnw.emoba.thatsapp.data.dateFromJSON
import fhnw.emoba.thatsapp.data.toJSONDateString
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*

class SystemMessageNewProfileImage(id: UUID, senderID: UUID, profileImageLink: String, date: LocalDateTime, metaInfo: String) : Message(id, senderID, date, metaInfo) {
    override var type = "system"
    override var subtype = "newProfileImage"
    var data = NewProfileImageData(profileImageLink)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getString("profileImageLink"),
                obj.getString("sendTime"),
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )
    constructor(id: UUID, senderID: UUID, profileImageLink: String, date: String, metaInfo: String) :
            this(id, senderID, profileImageLink, date.dateFromJSON(), metaInfo)
    constructor(senderID: UUID, profileImageLink: String, metaInfo: String) :
            this(UUID.randomUUID(), senderID, profileImageLink, LocalDateTime.now(), metaInfo)

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
                "sendTime": "${sendTime.toJSONDateString()}",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class NewProfileImageData(profileImageLink: String) {
        var profileImageLink = profileImageLink
    }
}