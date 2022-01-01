package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.constraintlayout.compose.ConstraintLayout
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.Drawer
import fhnw.emoba.thatsapp.ui.MenuIcon

@Composable
fun ChatListUI(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()

    with(model) {
        var title = if (isChatDetail) {
            "test"
        } else {
            Screens.CHATS.title
        }

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { ChatTopBar(model, title, scaffoldState) },
            drawerContent = { Drawer(model) }
        ) {

        }
    }
}

@Composable
private fun ChatTopBar(model: ThatsAppModel, title: String, scaffoldState: ScaffoldState) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { ChatMenuIcon(model, scaffoldState) }
    )
}

@Composable
private fun ChatMenuIcon(model: ThatsAppModel, scaffoldState: ScaffoldState) {
    with(model) {
        if (isChatDetail) {
            IconButton(onClick = { isChatDetail = false }) {
                Icon(Icons.Filled.ArrowBack, "Zurück")
            }
        } else {
            MenuIcon(scaffoldState)
        }
    }
}

@Composable
private fun ChatBody(model: ThatsAppModel) {
    with(model) {

    }
}