package fhnw.emoba.thatsapp

import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import fhnw.emoba.EmobaApp
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.AppUI


object ThatsApp : EmobaApp {

    override fun initialize(activity: ComponentActivity) {
        ThatsAppModel.connectAndSubscribe()
    }

    @ExperimentalAnimationApi
    @Composable
    override fun CreateUI() {
        AppUI(ThatsAppModel)
    }

}

