package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class MessageText(id: UUID, senderID: UUID, priority: Int, deleteItself: Boolean, text: String, metaInfo: String) : Message(id, senderID, metaInfo) {
    override var type = "message"
    override var subtype = "text"
    var data = TextData(priority, deleteItself, text)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getInt("priority"),
                obj.getJSONObject("data").getBoolean("deleteItself"),
                obj.getJSONObject("data").getString("text"),
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
                    "priority": ${data.priority},
                    "deleteItself": ${data.deleteItself},
                    "text": "${data.text}"
                },
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class TextData(priority: Int, deleteItself: Boolean, text: String) {
        var priority = priority
        var deleteItself = deleteItself
        var text = text
    }
}