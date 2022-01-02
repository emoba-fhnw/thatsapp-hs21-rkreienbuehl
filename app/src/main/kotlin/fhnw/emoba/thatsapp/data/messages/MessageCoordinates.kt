package fhnw.emoba.thatsapp.data.messages

import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*

class MessageCoordinates(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, lat: Number, lon: Number, date: LocalDateTime, metaInfo: String) :
    Message(id, senderID, date, metaInfo) {

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
                obj.getString("sendTime"),
                if (obj.has("metaInfo")) obj.getString("metaInfo") else ""
            )
    constructor(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, lat: Number, lon: Number, date: String, metaInfo: String) :
            this(id, senderID, priority, deletingItself, lat, lon, parseDateString(date), metaInfo)
    constructor(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, lat: Number, lon: Number, metaInfo: String) :
            this(id, senderID, priority, deletingItself, lat, lon, LocalDateTime.now(), metaInfo)

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
                "sendTime": "${getFormattedDateString()}",
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