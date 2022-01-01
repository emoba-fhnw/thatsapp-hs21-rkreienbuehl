package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.DefaultTopBar
import fhnw.emoba.thatsapp.ui.Drawer

@Composable
fun SettingsUI(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()

    with(model) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { DefaultTopBar(activeScreen.title, scaffoldState) },
            drawerContent = { Drawer(model) }
        ) {

        }
    }
}