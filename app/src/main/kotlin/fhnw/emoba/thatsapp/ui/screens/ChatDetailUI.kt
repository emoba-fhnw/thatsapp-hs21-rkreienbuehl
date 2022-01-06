package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.DefaultTopBar

@Composable
fun ChatDetailUI(model: ThatsAppModel, chatID: String?) {
    with(model) {
        Box(modifier = Modifier.background(Color.Red).fillMaxSize())
    }
}