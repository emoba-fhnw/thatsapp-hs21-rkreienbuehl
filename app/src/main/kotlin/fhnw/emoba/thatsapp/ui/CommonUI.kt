package fhnw.emoba.thatsapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel
import kotlinx.coroutines.launch

@Composable
fun DefaultTopBar(title: String, scaffoldState: ScaffoldState) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { MenuIcon(scaffoldState) }
        )
}

@Composable
fun MenuIcon(scaffoldState: ScaffoldState){
    val scope = rememberCoroutineScope()

    IconButton(onClick = { scope.launch{ scaffoldState.drawerState.open() } }) {
        Icon(Icons.Filled.Menu, "Drawer öffnen")
    }
}

@Composable
fun Drawer(model: ThatsAppModel, navController: NavHostController) {
    Column {
        for (screen in Screens.values()) {
            DrawerRow(model, navController, screen)
        }
    }
}

@Composable
private fun DrawerRow(model: ThatsAppModel, navController: NavHostController, screen: Screens) {
    with(model) {
        var color = if (model.activeScreen == screen) Color.LightGray else Color.White

        Row(verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier              = Modifier.fillMaxWidth().background(color)
                .padding(5.dp)){
            Icon(screen.icon, screen.title)
            Text(text     = screen.title,
                style    = MaterialTheme.typography.h5,
                modifier = Modifier.padding(5.dp)
                    .fillMaxWidth()
                    .clickable(onClick = {
                        activeScreen = screen
                        navController.navigate(screen.route)
                    }))
        }
        Divider()
    }
}

@Composable
fun ImageView(image: ImageBitmap?, modifier: Modifier) {
    if (image == null) {
        Box(modifier,
            contentAlignment = Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Image(image!!, "Bild", modifier = modifier)
    }
}