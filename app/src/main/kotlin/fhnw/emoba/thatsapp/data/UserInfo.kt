package fhnw.emoba.thatsapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import java.util.*

class UserInfo(
    id: UUID,
    username: String,
    profileImageLink: String
    ) {
    val id = id
    var username by mutableStateOf(username)
    var profileImageLink by mutableStateOf(profileImageLink)
    var userImage by mutableStateOf<ImageBitmap?>(null)
}