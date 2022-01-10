package fhnw.emoba.thatsapp

import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import fhnw.emoba.EmobaApp
import fhnw.emoba.thatsapp.data.CameraAppConnector
import fhnw.emoba.thatsapp.data.GPSConnector
import fhnw.emoba.thatsapp.data.ImageDownloadService
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.AppUI


object ThatsApp : EmobaApp {
    private lateinit var model: ThatsAppModel

    override fun initialize(activity: ComponentActivity) {
        val imageService = ImageDownloadService(activity)
        val cameraAppConnector = CameraAppConnector(activity)
        val gpsConnector = GPSConnector(activity)

        model = ThatsAppModel(imageService, cameraAppConnector, gpsConnector)
        model.connectAndSubscribe()
    }

    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @Composable
    override fun CreateUI() {
        AppUI(model)
    }

}

