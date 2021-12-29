package fhnw.emoba.thatsapp.data

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import fhnw.emoba.thatsapp.data.messages.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * ACHTUNG: Das ist nur eine erste Konfiguration eines Mqtt-Brokers.
 *
 * Dient vor allem dazu mit den verschiedenen Parametern experimentieren zu können
 *
 * siehe die Doku:
 * https://hivemq.github.io/hivemq-mqtt-client/
 * https://github.com/hivemq/hivemq-mqtt-client
 *
 * Ein generischer Mqtt-Client (gut um Messages zu kontrollieren)
 * http://www.hivemq.com/demos/websocket-client/
 *
 */
class MqttConnector (val mqttBroker: String,
                     val maintopic: String,
                     val qos: MqttQos = MqttQos.EXACTLY_ONCE){

    private val client = Mqtt5Client.builder()
        .serverHost(mqttBroker)
        .identifier(UUID.randomUUID().toString())
        .buildAsync()
    
    fun connectAndSubscribe(subtopic:           String = "",
                            onNewMessage:       (Message) -> Unit = {},
                            onError:      (Exception) -> Unit,
                            onConnectionFailed: () -> Unit = {}) {
        client.connectWith()
            .cleanStart(true)
            .keepAlive(30)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    onConnectionFailed.invoke()
                } else {
                    subscribe(subtopic, onNewMessage, onError)
                }
            }
    }

    fun subscribe(subtopic:     String = "",
                  onNewMessage: (Message) -> Unit,
                  onError:      (Exception) -> Unit,
    ){
        client.subscribeWith()
            .topicFilter(maintopic + subtopic)
            .qos(qos)
            .noLocal(true)
            .callback {
                try {
                    onNewMessage.invoke(it.payloadAsMessage())
                } catch (e: Exception) {
                    onError.invoke(e)
                }
            }
            .send()
    }

    fun publish(message:     Message,
                subtopic:    String = "",
                onPublished: () -> Unit = {},
                onError:     () -> Unit = {},
                retain:      Boolean = false
    ) {
        client.publishWith()
            .topic(maintopic + subtopic)
            .payload(message.asPayload())
            .qos(qos)
            .retain(retain)
            .messageExpiryInterval(120)
            .send()
            .whenComplete {_, throwable ->
                if(throwable != null){
                    onError.invoke()
                }
                else {
                    onPublished.invoke()
                }
             }
    }

    fun disconnect() {
        client.disconnectWith()
            .sessionExpiryInterval(0)
            .send()
    }
}

// Extension Functions
private fun Message.asPayload() : ByteArray = asJSON().toByteArray(StandardCharsets.UTF_8)
private fun Mqtt5Publish.payloadAsMessage() : Message {
    val obj = JSONObject(String(payloadAsBytes, StandardCharsets.UTF_8))
    val type = obj.getString("type")
    val subtype = obj.getString("subtype")
    return when {
        type.equals("system") && subtype.equals("connect") -> SystemMessageConnect(obj)
        type.equals("system") && subtype.equals("newUsername") -> SystemMessageNewUsername(obj)
        type.equals("system") && subtype.equals("newProfileImage") -> SystemMessageNewProfileImage(obj)
        type.equals("system") && subtype.equals("newChat") -> SystemMessageNewChat(obj)
        type.equals("system") && subtype.equals("leaveChat") -> SystemMessageLeaveChat(obj)
        type.equals("message") && subtype.equals("text") -> MessageText(obj)
        type.equals("message") && subtype.equals("image") -> MessageImage(obj)
        type.equals("message") && subtype.equals("coordinates") -> MessageCoordinates(obj)
        else -> throw IllegalArgumentException("Type or subtype unknown")
    }
}