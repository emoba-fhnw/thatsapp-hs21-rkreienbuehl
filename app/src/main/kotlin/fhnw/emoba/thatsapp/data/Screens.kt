package fhnw.emoba.thatsapp.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val title: String, val icon: ImageVector, val route: String) {
    CHATS("Chats", Icons.Filled.Chat, "chatList"),
    USERS("Benutzer", Icons.Filled.Person, "users"),
    SETTINGS("Einstellungen", Icons.Filled.Settings, "settings")
}