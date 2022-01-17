package fhnw.emoba.thatsapp.model

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.preference.PreferenceManager
import fhnw.emoba.thatsapp.data.*
import fhnw.emoba.thatsapp.data.messages.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class ThatsAppModel(activity: ComponentActivity, private val imageDownloadService: ImageDownloadService, private val cameraAppConnector: CameraAppConnector, private val gpsConnector: GPSConnector) {
    private val USER_ID_STRING = "USER-ID"
    private val USERNAME_STRING = "USERNAME"
    private val PROFILE_IMAGE_STRING = "PROFILE-IMAGE"

    private val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
    private val preferencesEditor = preferences.edit()

    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mqttBroker = "broker.hivemq.com"
    private val mainTopic = "fhnw/emoba/thatsapp/gruppe42"
    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic) }

    var activeScreen by mutableStateOf(Screens.CHATS)

    val chatInfos = mutableStateMapOf<String, ChatInfo>()
    val userInfos = mutableStateMapOf<String, UserInfo>()

    var photo by mutableStateOf<ImageBitmap?>(null)
    var dialogOpen by mutableStateOf(false)

    var ownUser: UserInfo

    init {
        val id: UUID
        var userID = preferences.getString(USER_ID_STRING, "")!!
        val username = preferences.getString(USERNAME_STRING, "ThatsApp User")!!
        val profileImage = preferences.getString(PROFILE_IMAGE_STRING, "")!!

        if (userID == "") {
            id = UUID.randomUUID()
            userID = id.toString()

            preferencesEditor.putString(USER_ID_STRING, userID).apply()
            preferencesEditor.putString(USERNAME_STRING, username).apply()
            preferencesEditor.putString(PROFILE_IMAGE_STRING, profileImage).apply()
        } else {
            id = UUID.fromString(userID)
        }

        ownUser = UserInfo(id, username, profileImage)

        userInfos[userID] = ownUser

        modelScope.launch {
            imageDownloadService.loadImage(
                ownUser.profileImageLink,
                onSuccess = {
                    ownUser.userImage = it
                },
                onError = {
                    ownUser.userImage = it
                }
            )
        }
    }

    fun getDefaultImage(): ImageBitmap {
        return imageDownloadService.getDefaultImage()
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
            },
            onSubscribeSuccess = {
                Log.d("DEBUG", "Connection successful")
                sendConnectMessage()
            }
        )
    }

    private fun sendConnectMessage() {
        val message = SystemMessageConnect(ownUser.id, ownUser.username, "connect", ownUser.profileImageLink, "")
        mqttConnector.publish(message, onPublished = { Log.d("INFO", "Connect wurde versendet") })
    }

    fun changeUsername(username: String) {
        preferencesEditor.putString(USERNAME_STRING, username).apply()

        ownUser.username = username
        val message = SystemMessageNewUsername(ownUser.id, username, "")
        mqttConnector.publish(message, onPublished = { Log.d("INFO", "Änderung des Usernamens wurde versendet") })
    }

    fun changeProfileImage(image: ImageBitmap) {
        modelScope.launch {
            uploadBitmap(
                bitmap = image.asAndroidBitmap(),
                onSuccess = {
                    updateProfileImage(image, it)
                }
            )
        }
    }

    private fun updateProfileImage(image: ImageBitmap, imageLink: String) {
        preferencesEditor.putString(PROFILE_IMAGE_STRING, imageLink).apply()

        ownUser.userImage = image
        ownUser.profileImageLink = imageLink

        val message = SystemMessageNewProfileImage(ownUser.id, imageLink, "")
        mqttConnector.publish(message, onPublished = { Log.d("INFO", "Änderung des Profilbildes wurde versendet") })
    }

    fun createNewChat(userList: List<String>, chatImage: ImageBitmap? = null, onPublished: (chatID: String) -> Unit) {
        val users = mutableListOf(ownUser.id.toString())

        for (user in userList) {
            users.add(user)
        }

        if (chatImage != null) {
            modelScope.launch {
                uploadBitmap(
                    chatImage.asAndroidBitmap(),
                    onSuccess = {
                        newChat(users, it, onPublished)
                    },
                    onError = { _, _ ->
                        newChat(users, onPublished = onPublished)
                    }
                )
            }
        } else {
            newChat(users, onPublished = onPublished)
        }
    }

    private fun newChat(userList: List<String>, chatImageLink: String = "", onPublished: (chatID: String) -> Unit) {
        val message = SystemMessageNewChat(ownUser.id, chatImageLink, userList, "")

        mqttConnector.publish(
            message,
            onPublished = {
                handleNewChat(message)
                onPublished.invoke(message.data.chatID.toString())
            })
    }

    fun sendTextMessage(text: String, chatInfo: ChatInfo) {
        val message = MessageText(ownUser.id, 1, false, text, "")

        mqttConnector.publish(message, chatInfo.id.toString(), onPublished = { chatInfo.messages.add(message) })
    }

    fun sendPositionMessage(chatInfo: ChatInfo) {
        gpsConnector.getLocation(
            onSuccess = {
                val message = MessageCoordinates(ownUser.id, 1, false, it.latitude, it.longitude, "")

                mqttConnector.publish(message, chatInfo.id.toString(), onPublished = {
                    chatInfo.messages.add(message)

                    imageDownloadService.loadImage(
                        message.mapsLink(),
                        onSuccess = {
                            Log.d("DEBUG", "$it")
                            message.data.image = it
                        },
                        onError = {
                            message.data.image = it
                        }
                    )
                })
            },
            onFailure = {
                Log.d("ERROR", it.stackTraceToString())
            },
            onPermissionDenied = {
                Log.d("DEBUG", "Permissions refused")
            })
    }

    fun takePhoto() {
        // photo = null
        cameraAppConnector.getBitmap(
            onSuccess  = {
                photo = it.asImageBitmap()
                dialogOpen = true
            },
            onCanceled = {
                Log.d("DEBUG", "Kein neues Bild")
            })
    }

    fun uploadAndSendImage(chatInfo: ChatInfo) {
        modelScope.launch {
            uploadBitmap(
                photo!!.asAndroidBitmap(),
                onSuccess = {
                    val message = MessageImage(ownUser.id, 1, false, it, "")
                    photo = null

                    mqttConnector.publish(
                        message,
                        chatInfo.id.toString(),
                        onPublished = {
                            chatInfo.messages.add(message)
                            imageDownloadService.loadImage(
                                message.data.imageLink,
                                onSuccess = { image ->
                                    message.data.image = image
                                },
                                onError = { image ->
                                    message.data.image = image
                                }
                            )
                        })
                },
                onError = { code, msg ->
                    Log.d("ERROR", "$code - Upload fehlgeschlagen: $msg")
                    photo = null
                }
            )
        }
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
            if (user == ownUser) return

            user.username = message.data.username
            user.profileImageLink = message.data.profileImageLink

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

        if (message.subtype == "connect") {
            val msg = SystemMessageConnect(ownUser.id, ownUser.username, "answerConnect", ownUser.profileImageLink, "")
            mqttConnector.publish(msg, onPublished = { Log.d("INFO", "answerConnect wurde versendet") })
        }

        modelScope.launch {
            imageDownloadService.loadImage(
                user.profileImageLink,
                onSuccess = {
                    user.userImage = it
                },
                onError = {
                    user.userImage = it
                }
            )
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

            modelScope.launch {
                imageDownloadService.loadImage(
                    user.profileImageLink,
                    onSuccess = {
                        user.userImage = it
                    },
                    onError = {
                        user.userImage = it
                    }
                )
            }

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
                imageDownloadService.loadImage(
                    chatInfo.chatImageLink,
                    onSuccess = {
                        chatInfo.chatImage = it
                        chatInfo.imageLoadingFailed = false
                    },
                    onError = {
                        chatInfo.chatImage = it
                        chatInfo.imageLoadingFailed = true
                    }
                )
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
                imageDownloadService.loadImage(
                    message.data.imageLink,
                    onSuccess = {
                        message.data.image = it
                    },
                    onError = {
                        message.data.image = it
                    }
                )
            }
        } else {
            Log.d("ERROR", "Neue Bildnachricht auf falschem Chat")
        }
    }

    private fun handleCoordinatesMessage(message: MessageCoordinates, chatInfo: ChatInfo?) {
        if (chatInfo != null) {
            Log.d("DEBUG", "Neue Koordinatennachricht auf Chat ${chatInfo.id}")

            chatInfo.messages.add(message)

            modelScope.launch {
                Log.d("DEBUG", message.mapsLink())
                imageDownloadService.loadImage(
                    message.mapsLink(),
                    onSuccess = {
                        Log.d("DEBUG", "$it")
                        message.data.image = it
                    },
                    onError = {
                        message.data.image = it
                    }
                )
            }
        } else {
            Log.d("ERROR", "Neue Koordinatennachricht auf falschem Chat")
        }
    }
}
