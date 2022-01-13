package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.data.UserInfo
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.DefaultTopBar
import fhnw.emoba.thatsapp.ui.Drawer
import fhnw.emoba.thatsapp.ui.ImageView

@Composable
fun UserListUI(model: ThatsAppModel, navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()

    with(model) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { DefaultTopBar("Benutzer", scaffoldState) },
            drawerContent = { Drawer(model, navController) }
        ) {
            UserListBody(model = model, navController = navController)
        }
    }
}

@Composable
private fun UserListBody(model: ThatsAppModel, navController: NavHostController) {
    with(model) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                items(userInfos.values.filter { it != ownUser }) {
                    UserListRow(model = model, userInfo = it, navController = navController)
                }
            }
        }
    }
}

@Composable
private fun UserListRow(model: ThatsAppModel, userInfo: UserInfo, navController: NavHostController) {
    with(model) {
        ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
        ) {
            val (image, username, button) = createRefs()

            Box(modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    bottom.linkTo(parent.bottom, margin = 10.dp)
                    height = Dimension.fillToConstraints
                } .aspectRatio(1f)) {
                ImageView(image = userInfo.userImage, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = userInfo.username,
                modifier = Modifier.constrainAs(username) {
                    top.linkTo(parent.top, margin = 10.dp)
                    bottom.linkTo(parent.bottom, margin = 10.dp)
                    start.linkTo(image.end, margin = 10.dp)
                },
                style = MaterialTheme.typography.h6
            )
            Button(
                onClick = {
                    createNewChat(listOf(userInfo.id.toString()), onPublished = {
                        activeScreen = Screens.CHATS
                        navController.navigate("chat/$it")
                    })
                },
                modifier = Modifier.constrainAs(button) {
                    top.linkTo(parent.top, margin = 20.dp)
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                }
            ) {
                Text(text = "Neuer Chat")
            }
        }
        Divider()
    }
}