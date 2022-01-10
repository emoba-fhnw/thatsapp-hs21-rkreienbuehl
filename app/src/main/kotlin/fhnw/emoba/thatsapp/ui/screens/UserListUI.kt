package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.DefaultTopBar
import fhnw.emoba.thatsapp.ui.Drawer

@Composable
fun UserListUI(model: ThatsAppModel, navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()

    with(model) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { DefaultTopBar("Benutzer", scaffoldState) },
            drawerContent = { Drawer(model, navController) }
        ) {
            Text("Benutzerliste")
        }
    }
}