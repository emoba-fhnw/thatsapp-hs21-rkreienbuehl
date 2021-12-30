package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class MessageImage(id: UUID, senderID: UUID, priority: Int, deleteItself: Boolean, imageLink: String, metaInfo: String) : Message(id, senderID, metaInfo) {
    override var type = "message"
    override var subtype = "image"
    var data = ImageData(priority, deleteItself, imageLink)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getInt("priority"),
                obj.getJSONObject("data").getBoolean("deleteItself"),
                obj.getJSONObject("data").getString("imageLink"),
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
                    "imageLink": "${data.imageLink}"
                },
                "sendTime": "$sendTime",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class ImageData(priority: Int, deleteItself: Boolean, imageLink: String) {
        var priority = priority
        var deleteItself = deleteItself
        var imageLink = imageLink
    }
}