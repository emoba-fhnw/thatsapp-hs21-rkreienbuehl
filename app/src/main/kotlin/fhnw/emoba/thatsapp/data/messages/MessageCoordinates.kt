package fhnw.emoba.thatsapp.data.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import fhnw.emoba.thatsapp.data.GeoPosition
import fhnw.emoba.thatsapp.data.dateFromJSON
import fhnw.emoba.thatsapp.data.toJSONDateString
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*

class MessageCoordinates(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, lat: Double, lon: Double, date: LocalDateTime, metaInfo: String) :
    Message(id, senderID, date, metaInfo, false) {

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
    constructor(id: UUID, senderID: UUID, priority: Int, deletingItself: Boolean, lat: Double, lon: Double, date: String, metaInfo: String) :
            this(id, senderID, priority, deletingItself, lat, lon, date.dateFromJSON(), metaInfo)
    constructor(senderID: UUID, priority: Int, deletingItself: Boolean, lat: Double, lon: Double, metaInfo: String) :
            this(UUID.randomUUID(), senderID, priority, deletingItself, lat, lon, LocalDateTime.now(), metaInfo)

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
                "sendTime": "${sendTime.toJSONDateString()}",
                "metaInfo": "$metaInfo"
            }
        """.trimIndent()
    }

    inner class CoordinatesData(priority: Int, deletingItself: Boolean, lat: Double, lon: Double) {
        var priority = priority
        var deletingItself = deletingItself
        var lat = lat
        var lon = lon
        var geoCoordinates = GeoPosition(lon, lat, 0.0)
        var image by mutableStateOf<ImageBitmap?>(null)
    }

    fun mapsLink(): String {
        return "https://maps.googleapis.com/maps/api/staticmap?center=${data.lat},${data.lon}&zoom=14&size=400x400&scale=2&markers=color:red|${data.lat},${data.lon}&key=AIzaSyBld5kg_sRVjEZprODY8mNWgmu98goMzpk"
    }
}