package fhnw.emoba.thatsapp.ui


import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.screens.ChatListUI
import fhnw.emoba.thatsapp.ui.screens.SettingsUI
import fhnw.emoba.thatsapp.ui.screens.UserListUI


@ExperimentalAnimationApi
@Composable
fun AppUI(model : ThatsAppModel){
    with(model) {
        MaterialTheme {
            Crossfade(targetState = activeScreen) {
                when (it) {
                    Screens.CHATS -> { ChatListUI(model) }
                    Screens.USERS -> { UserListUI(model) }
                    Screens.SETTINGS -> { SettingsUI(model) }
                }
            }
        }
    }
}
