package fhnw.emoba.thatsapp.data.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import fhnw.emoba.thatsapp.data.dateFromJSON
import fhnw.emoba.thatsapp.data.toJSONDateString
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*

class MessageImage(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, imageLink: String, date: LocalDateTime, metaInfo: String) : Message(id, senderID, date, metaInfo, false) {
    override var type = "message"
    override var subtype = "image"
    var data = ImageData(priority, deletingItself, imageLink)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getInt("priority"),
                obj.getJSONObject("data").getBoolean("deletingItself"),
                obj.getJSONObject("data").getString("imageLink"),
                obj.getString("sendTime"),
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )
    constructor(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, imageLink: String, date: String, metaInfo: String) :
            this(id, senderID, priority, deletingItself, imageLink, date.dateFromJSON(), metaInfo)
    constructor(senderID: UUID, priority: Int, deletingItself: Boolean, imageLink: String, metaInfo: String) :
            this(UUID.randomUUID(), senderID, priority, deletingItself, imageLink, LocalDateTime.now(), metaInfo)

    override fun asJSON(): String {
        return """
            {
                "id": "$id",
                "senderID": "$senderID",
                "type": "$type",
                "subtype": "$subtype",
                "data": {
                    "priority": ${data.priority},
                    "deletingItself": ${data.deletingItself},
                    "imageLink": "${data.imageLink}"
                },
                "sendTime": "${sendTime.toJSONDateString()}",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class ImageData(priority: Int, deletingItself: Boolean, imageLink: String) {
        var priority = priority
        var deletingItself = deletingItself
        var imageLink = imageLink
        var image by mutableStateOf<ImageBitmap?>(null)
    }
}