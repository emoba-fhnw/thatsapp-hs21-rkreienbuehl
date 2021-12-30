package fhnw.emoba.thatsapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import fhnw.emoba.thatsapp.data.messages.Message
import java.time.LocalDateTime
import java.util.*

class ChatInfo(
    id: UUID,
    chatImageLink: String,
    memberList: List<UserInfo>,
    messageList: List<Message>,
    lastMessage: LocalDateTime
    ) {
    val id = id
    var chatImageLink by mutableStateOf(chatImageLink)
    var members = mutableStateListOf<UserInfo>()
    var messages = mutableStateListOf<Message>()
    var lastMessage = mutableStateOf(lastMessage)

    init {
        for (member in memberList) {
            members.add(member)
        }

        for (message in messageList) {
            messages.add(message)
        }
    }
}