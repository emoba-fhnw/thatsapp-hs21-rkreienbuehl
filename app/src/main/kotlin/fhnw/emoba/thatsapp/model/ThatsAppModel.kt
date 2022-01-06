package fhnw.emoba.thatsapp.model

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.data.ChatInfo
import fhnw.emoba.thatsapp.data.MqttConnector
import fhnw.emoba.thatsapp.data.UserInfo
import fhnw.emoba.thatsapp.data.messages.*
import java.time.LocalDateTime
import java.util.*

object ThatsAppModel {
    var title = "Hello ThatsApp"

    var activeScreen by mutableStateOf(Screens.CHATS)
    var isChatDetail by mutableStateOf(false)

    var ownUser = UserInfo(UUID.fromString("fe79df56-a84b-4029-ae02-bbc08a6e9ed5"), "Roger", "")

    private const val mqttBroker    = "broker.hivemq.com"
    private const val mainTopic     = "fhnw/emoba/flutterapp"
    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic) }

    val chatInfos = mutableStateListOf<ChatInfo>()
    val userInfos = mutableStateMapOf<String, UserInfo>()

    init {
        userInfos[ownUser.id.toString()] = ownUser
    }

    fun connectAndSubscribe(){
        Log.d("INFO", "Benutzer-ID: ${ownUser.id}")

        mqttConnector.connectAndSubscribe(
            onNewMessage = {
                handleIncomingMessage(it)
            },
            onError = {
                Log.d("ERROR", it.toString())
            },
            onConnectionFailed = {
                Log.d("ERROR", "Connection failed")
                connectAndSubscribe()
            }
        )
    }

    fun publish(){

    }

    private fun handleIncomingMessage(message: Message, chatInfo: ChatInfo? = null) {
        when (message) {
            is SystemMessageConnect -> handleConnect(message)
            is SystemMessageNewUsername -> handleNewUsername(message)
            is SystemMessageNewProfileImage -> handleNewProfileImage(message)
            is SystemMessageNewChat -> handleNewChat(message)
            is SystemMessageLeaveChat -> handleLeaveChat(message, chatInfo)
            is MessageText -> handleTextMessage(message, chatInfo)
            is MessageImage -> handleImageMessage(message, chatInfo)
            is MessageCoordinates -> handleCoordinatesMessage(message, chatInfo)
        }
    }

    private fun handleConnect(message: SystemMessageConnect) {
        val user = userInfos[message.senderID.toString()]

        if (user != null) {
            user.username = message.data.username
            user.profileImageLink = message.data.profileImageLink

            Log.d("DEBUG", "handleConnect: Daten von Benutzer ${message.data.username} (${message.senderID}) aktualisiert")
        } else {
            userInfos[message.senderID.toString()] =
                UserInfo(
                    message.senderID,
                    message.data.username,
                    message.data.profileImageLink
                )

            Log.d("DEBUG", "handleConnect: Neuen Benutzer ${message.data.username} (${message.senderID}) hinzugefügt")
        }
    }

    private fun handleNewUsername(message: SystemMessageNewUsername) {
        val user = userInfos[message.senderID.toString()]

        if (user != null) {
            user.username = message.data.username

            Log.d("DEBUG", "handleNewUsername: Benutzername von ${user.username} (${user.id}) aktualisiert")
        } else {
            Log.d("ERROR", "handleNewUsername: Benutzer ${message.data.username} (${message.senderID}) nicht bekannt.")
        }
    }

    private fun handleNewProfileImage(message: SystemMessageNewProfileImage) {
        val user = userInfos[message.senderID.toString()]

        if (user != null) {
            user.profileImageLink = message.data.profileImageLink

            Log.d("DEBUG", "handleNewProfileImage: Profilbild Link von ${user.username} (${user.id}) aktualisiert")
        } else {
            Log.d("ERROR", "handleNewProfileImage: Benutzer ${message.senderID} nicht bekannt.")
        }
    }

    private fun handleNewChat(message: SystemMessageNewChat) {
        if (message.data.members.contains(ownUser.id.toString())) {
            Log.d("DEBUG", "handleNewChat: Neuer Chat - ID: ${message.data.chatID}")

            val members = mutableListOf<UserInfo>()

            for (member in message.data.members) {
                val user = userInfos[member]
                if (user != null) {
                    members.add(user)
                } else {
                    Log.d("ERROR", "handleNewChat: Benutzer $member nicht bekannt.")
                }
            }

            val chatID = message.data.chatID.toString()

            val chatInfo = ChatInfo(message.data.chatID, message.data.chatImageLink, members, listOf(), LocalDateTime.now(), userInfos[message.senderID.toString()]!!)
            chatInfos.add(chatInfo)

            mqttConnector.subscribe(chatID,
                onNewMessage = {
                    handleIncomingMessage(it, chatInfo)
                },
                onError = {
                    Log.d("ERROR", it.toString())
                }
            )
        } else {
            Log.d("DEBUG", "handleNewChat: Neuer Chat - ID: ${message.data.chatID} nicht für eigenen Benutzer")
        }
    }

    private fun handleLeaveChat(message: SystemMessageLeaveChat, chatInfo: ChatInfo?) {
        val userID = message.senderID.toString()
        val userInfo = userInfos[userID]

        if (userInfo != null && chatInfo != null) {
            Log.d("DEBUG", "handleLeaveChat: Benutzer ${userInfo.username} hat den Chat verlassen - chatID: ${chatInfo.id}")

            chatInfo.members.remove(userInfo)
            chatInfo.messages.add(message)
        } else {
            Log.d("ERROR", "handleLeaveChat: Chat oder Benutzer nicht bekannt.")
        }
    }

    private fun handleTextMessage(message: MessageText, chatInfo: ChatInfo?) {
        if (chatInfo != null) {
            Log.d("DEBUG", "Neue Textnachricht auf Chat ${chatInfo.id}")

            chatInfo.messages.add(message)
            chatInfo.lastMessage = message.sendTime
            chatInfo.lastMessageSender = userInfos[message.senderID.toString()]!!
        } else {
            Log.d("ERROR", "Neue Textnachricht auf falschem Chat")
        }

    }

    private fun handleImageMessage(message: MessageImage, chatInfo: ChatInfo?) {
        if (chatInfo != null) {
            Log.d("DEBUG", "Neue Bildnachricht auf Chat ${chatInfo.id}")

            chatInfo.messages.add(message)
            chatInfo.lastMessage = message.sendTime
            chatInfo.lastMessageSender = userInfos[message.senderID.toString()]!!
        } else {
            Log.d("ERROR", "Neue Bildnachricht auf falschem Chat")
        }
    }

    private fun handleCoordinatesMessage(message: MessageCoordinates, chatInfo: ChatInfo?) {
        if (chatInfo != null) {
            Log.d("DEBUG", "Neue Koordinatennachricht auf Chat ${chatInfo.id}")

            chatInfo.messages.add(message)
            chatInfo.lastMessage = message.sendTime
            chatInfo.lastMessageSender = userInfos[message.senderID.toString()]!!
        } else {
            Log.d("ERROR", "Neue Koordinatennachricht auf falschem Chat")
        }
    }
}