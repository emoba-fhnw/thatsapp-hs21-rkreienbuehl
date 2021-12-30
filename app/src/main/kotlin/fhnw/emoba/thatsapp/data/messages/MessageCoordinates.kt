package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.util.*

class MessageCoordinates(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, lat: Number, lon: Number, metaInfo: String) :
    Message(id, senderID, metaInfo) {

    override var type = "message"
    override var subtype = "coordinates"
    var data = CoordinatesData(priority, deletingItself, lat, lon)

    constructor(obj: JSONObject) :
            this(
                UUID.fromString(obj.getString("id")),
                UUID.fromString(obj.getString("senderID")),
                obj.getJSONObject("data").getInt("priority"),
                obj.getJSONObject("data").getBoolean("deletingItself"),
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
                    "deletingItself": ${data.deletingItself},
                    "lat": ${data.lat},
                    "lon": ${data.lon}
                },
                "sendTime": "$sendTime",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class CoordinatesData(priority: Int, deletingItself: Boolean, lat: Number, lon: Number) {
        var priority = priority
        var deletingItself = deletingItself
        var lat = lat
        var lon = lon
    }
}