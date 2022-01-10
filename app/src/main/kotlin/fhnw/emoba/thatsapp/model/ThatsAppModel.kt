package fhnw.emoba.thatsapp.model

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import fhnw.emoba.thatsapp.data.*
import fhnw.emoba.thatsapp.data.messages.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class ThatsAppModel(private val imageDownloadService: ImageDownloadService, private val cameraAppConnector: CameraAppConnector, private val gpsConnector: GPSConnector) {
    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var activeScreen by mutableStateOf(Screens.CHATS)
    var isChatDetail by mutableStateOf(false)

    var ownUser = UserInfo(UUID.fromString("fe79df56-a84b-4029-ae02-bbc08a6e9ed5"), "Roger", "")

    private val mqttBroker = "broker.hivemq.com"
    private val mainTopic = "fhnw/emoba/flutterapp"
    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic) }

    val chatInfos = mutableStateMapOf<String, ChatInfo>()
    val userInfos = mutableStateMapOf<String, UserInfo>()

    var photo by mutableStateOf<ImageBitmap?>(null)
    var photoDialogOpen by mutableStateOf(false)

    init {
        userInfos[ownUser.id.toString()] = ownUser
    }

    fun connectAndSubscribe() {
        Log.d("INFO", "Benutzer-ID: ${ownUser.id}")

        mqttConnector.connectAndSubscribe(
            onNewMessage = {
                handleIncomingMessage(it)
            },
            onError = {
                Log.d("ERROR", it.stackTraceToString())
            },
            onConnectionFailed = {
                Log.d("ERROR", "Connection failed")
                connectAndSubscribe()
            }
        )
    }

    fun sendTextMessage(text: String, chatInfo: ChatInfo) {
        val message = MessageText(ownUser.id, 1, false, text, "")

        mqttConnector.publish(message, chatInfo.id.toString(), onPublished = { chatInfo.messages.add(message) })
    }

    fun sendPositionMessage(chatInfo: ChatInfo) {
        gpsConnector.getLocation(
            onSuccess = {
                val message = MessageCoordinates(ownUser.id, 1, false, it.latitude, it.longitude, "")

                mqttConnector.publish(message, chatInfo.id.toString(), onPublished = { chatInfo.messages.add(message) })
            },
            onFailure = {
                Log.d("ERROR", it.stackTraceToString())
            },
            onPermissionDenied = {
                Log.d("DEBUG", "Permissions refused")
            })
    }

    fun takePhoto() {
        photo = null
        cameraAppConnector.getBitmap(
            onSuccess  = {
                photo = it.asImageBitmap()
                photoDialogOpen = true
            },
            onCanceled = {
                Log.d("DEBUG", "Kein neues Bild")
            })
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
        var user = userInfos[message.senderID.toString()]

        if (user != null) {
            user.username = message.data.username
            user.profileImageLink = message.data.profileImageLink
            user.isLoading = true

            Log.d(
                "DEBUG",
                "handleConnect: Daten von Benutzer ${message.data.username} (${message.senderID}) aktualisiert"
            )
        } else {
            user = UserInfo(
                message.senderID,
                message.data.username,
                message.data.profileImageLink
            )

            userInfos[message.senderID.toString()] = user

            Log.d(
                "DEBUG",
                "handleConnect: Neuen Benutzer ${message.data.username} (${message.senderID}) hinzugefügt"
            )
        }

        modelScope.launch {
            user.userImage = imageDownloadService.loadImage(user.profileImageLink)
            user.isLoading = false
        }
    }

    private fun handleNewUsername(message: SystemMessageNewUsername) {
        val user = userInfos[message.senderID.toString()]

        if (user != null) {
            user.username = message.data.username

            Log.d(
                "DEBUG",
                "handleNewUsername: Benutzername von ${user.username} (${user.id}) aktualisiert"
            )
        } else {
            Log.d(
                "ERROR",
                "handleNewUsername: Benutzer ${message.data.username} (${message.senderID}) nicht bekannt."
            )
        }
    }

    private fun handleNewProfileImage(message: SystemMessageNewProfileImage) {
        val user = userInfos[message.senderID.toString()]

        if (user != null) {
            user.profileImageLink = message.data.profileImageLink

            Log.d(
                "DEBUG",
                "handleNewProfileImage: Profilbild Link von ${user.username} (${user.id}) aktualisiert"
            )
        } else {
            Log.d("ERROR", "handleNewProfileImage: Benutzer ${message.senderID} nicht bekannt.")
        }
    }

    private fun handleNewChat(message: SystemMessageNewChat) {
        if (message.data.members.contains(ownUser.id.toString())) {
            Log.d("DEBUG", "handleNewChat: Neuer Chat - ID: ${message.data.chatID}")

            val chatID = message.data.chatID.toString()

            if (chatInfos[chatID] != null) {
                Log.d("ERROR", "Chat mit ID $chatID ist bereits vorhanden")
                return
            }

            val members = mutableListOf<UserInfo>()

            for (member in message.data.members) {
                val user = userInfos[member]
                if (user != null) {
                    members.add(user)
                } else {
                    Log.d("ERROR", "handleNewChat: Benutzer $member nicht bekannt.")
                }
            }

            val chatInfo = ChatInfo(
                message.data.chatID,
                message.data.chatImageLink,
                members,
                listOf(message)
            )

            chatInfos[chatID] = chatInfo

            mqttConnector.subscribe(chatID,
                onNewMessage = {
                    handleIncomingMessage(it, chatInfo)
                },
                onError = {
                    Log.d("ERROR", it.stackTraceToString())
                }
            )

            modelScope.launch {
                chatInfo.chatImage = imageDownloadService.loadImage(chatInfo.chatImageLink)
                chatInfo.isLoading = false
            }


        } else {
            Log.d(
                "DEBUG",
                "handleNewChat: Neuer Chat - ID: ${message.data.chatID} nicht für eigenen Benutzer"
            )
        }
    }

    private fun handleLeaveChat(message: SystemMessageLeaveChat, chatInfo: ChatInfo?) {
        val userID = message.senderID.toString()
        val userInfo = userInfos[userID]

        if (userInfo != null && chatInfo != null) {
            Log.d(
                "DEBUG",
                "handleLeaveChat: Benutzer ${userInfo.username} hat den Chat verlassen - chatID: ${chatInfo.id}"
            )

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
        } else {
            Log.d("ERROR", "Neue Textnachricht auf falschem Chat")
        }

    }

    private fun handleImageMessage(message: MessageImage, chatInfo: ChatInfo?) {
        if (chatInfo != null) {
            Log.d("DEBUG", "Neue Bildnachricht auf Chat ${chatInfo.id}")

            chatInfo.messages.add(message)

            modelScope.launch {
                message.data.image = imageDownloadService.loadImage(message.data.imageLink)
            }
        } else {
            Log.d("ERROR", "Neue Bildnachricht auf falschem Chat")
        }
    }

    private fun handleCoordinatesMessage(message: MessageCoordinates, chatInfo: ChatInfo?) {
        if (chatInfo != null) {
            Log.d("DEBUG", "Neue Koordinatennachricht auf Chat ${chatInfo.id}")

            chatInfo.messages.add(message)
        } else {
            Log.d("ERROR", "Neue Koordinatennachricht auf falschem Chat")
        }
    }
}
