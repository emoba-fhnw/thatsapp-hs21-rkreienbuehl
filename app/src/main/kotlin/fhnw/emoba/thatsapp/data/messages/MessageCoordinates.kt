package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class MessageCoordinates(id: UUID, senderID: UUID, priority: Int, deleteItself: Boolean, lat: Number, lon: Number, metaInfo: String) :
    Message(id, senderID, metaInfo) {

    override var type = "message"
    override var subtype = "coordinates"
    var data = CoordinatesData(priority, deleteItself, lat, lon)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getInt("priority"),
                obj.getJSONObject("data").getBoolean("deleteItself"),
                obj.getJSONObject("data").getDouble("lat"),
                obj.getJSONObject("data").getDouble("lon"),
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
                    "lat": ${data.lat},
                    "lon": ${data.lon}
                },
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class CoordinatesData(priority: Int, deleteItself: Boolean, lat: Number, lon: Number) {
        var priority = priority
        var deleteItself = deleteItself
        var lat = lat
        var lon = lon
    }
}