package fhnw.emoba.thatsapp

import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import fhnw.emoba.EmobaApp
import fhnw.emoba.thatsapp.data.ImageService
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.AppUI


object ThatsApp : EmobaApp {
    private lateinit var model: ThatsAppModel

    override fun initialize(activity: ComponentActivity) {
        val imageService = ImageService(activity)
        model = ThatsAppModel(imageService)
        model.connectAndSubscribe()
    }

    @ExperimentalAnimationApi
    @Composable
    override fun CreateUI() {
        AppUI(model)
    }

}

