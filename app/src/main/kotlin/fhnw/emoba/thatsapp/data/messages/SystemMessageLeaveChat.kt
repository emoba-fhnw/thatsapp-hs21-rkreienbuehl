package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class SystemMessageLeaveChat(id: UUID, senderID: UUID, metaInfo: String) : Message(id, senderID, metaInfo) {
    override var type = "system"
    override var subtype = "leaveChat"

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )

    override fun asJSON(): String {
        return """
            {
                "id": "$id",
                "senderID": "$senderID",
                "type": "$type",
                "subtype": "$subtype",
                "data": {},
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }
}