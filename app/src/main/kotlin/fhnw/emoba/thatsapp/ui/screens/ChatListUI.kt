package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fhnw.emoba.thatsapp.data.ChatInfo
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.data.messages.MessageCoordinates
import fhnw.emoba.thatsapp.data.messages.MessageImage
import fhnw.emoba.thatsapp.data.messages.MessageText
import fhnw.emoba.thatsapp.data.toDateString
import fhnw.emoba.thatsapp.data.toTimeString
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.Drawer
import fhnw.emoba.thatsapp.ui.MenuIcon

@ExperimentalAnimationApi
@Composable
fun ChatListUI(model: ThatsAppModel) {
    val scaffoldState = rememberScaffoldState()
    val navController = rememberAnimatedNavController()

    with(model) {
        var title = if (isChatDetail) {
            "test"
        } else {
            Screens.CHATS.title
        }

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { ChatTopBar(model, title, navController, scaffoldState) },
            drawerContent = { Drawer(model) }
        ) {
            AnimatedNavHost(navController, startDestination = "chatList") {
                composable(
                    "chatList",
                    enterTransition = {
                        when (initialState.destination.route) {
                            "chat/{chatID}" ->
                                slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
                            else -> null
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            "chat/{chatID}" ->
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
                            else -> null
                        }
                    }
                ) { ChatListBody(model, navController) }
                composable(
                    "chat/{chatID}",
                    enterTransition = {
                        when (initialState.destination.route) {
                            "chatList" ->
                                slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
                            else -> null
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            "chatList" ->
                                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
                            else -> null
                        }
                    }
                ) { backStackEntry -> ChatDetailUI(model, backStackEntry.arguments?.getString("chatID")) }
            }
        }
    }
}

@Composable
private fun ChatTopBar(model: ThatsAppModel, title: String, navController: NavController, scaffoldState: ScaffoldState) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { ChatMenuIcon(model, navController, scaffoldState) }
    )
}

@Composable
private fun ChatMenuIcon(model: ThatsAppModel, navController: NavController, scaffoldState: ScaffoldState) {
    with(model) {
        if (isChatDetail) {
            IconButton(onClick = {
                navController.navigateUp()
                isChatDetail = false
            }) {
                Icon(Icons.Filled.ArrowBack, "Zurück")
            }
        } else {
            MenuIcon(scaffoldState)
        }
    }
}

@Composable
private fun ChatListBody(model: ThatsAppModel, navController: NavController) {
    with(model) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                items(chatInfos.sortedBy { it.lastMessage } .reversed()) {
                    ChatListRow(model, it, navController)
                }
            }
        }
    }
}

@Composable
private fun ChatListRow(model: ThatsAppModel, chatInfo: ChatInfo, navController: NavController) {
    with(model) {
        ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
            .clickable(onClick = {
                navController.navigate("chat/${chatInfo.id}")
                isChatDetail = true
            })
        ) {
            val (image, text, lastMessageDate, lastMessageTime, lastMessageSender, lastMessage) = createRefs()
            val chatTitle = chatInfo.members.filter { it != model.ownUser }.map { it.username } .joinToString(", ")


            val lastMsgText = if (chatInfo.messages.size == 0) {
                "hat neuen Chat erstellt"
            } else {

                when (val lastMsg = chatInfo.messages.last()) {
                    is MessageText -> lastMsg.data.text
                    is MessageImage -> "hat ein Bild gesendet"
                    is MessageCoordinates -> "hat Standortdaten geteilt"
                    else -> ""
                }
            }

            Text(
                text = chatTitle,
                modifier = Modifier.constrainAs(text) {
                    start.linkTo(parent.start, margin = 10.dp)
                    top.linkTo(parent.top, margin = 10.dp)
                },
                style = MaterialTheme.typography.h6
            )
            Text(
                text = chatInfo.lastMessage.toDateString(),
                modifier = Modifier.constrainAs(lastMessageDate) {
                    end.linkTo(parent.end, margin = 10.dp)
                    top.linkTo(parent.top, margin = 10.dp)
                },
                style = MaterialTheme.typography.body2
            )
            Text(
                text = chatInfo.lastMessage.toTimeString(),
                modifier = Modifier.constrainAs(lastMessageTime) {
                    end.linkTo(parent.end, margin = 10.dp)
                    top.linkTo(lastMessageDate.bottom)
                },
                style = MaterialTheme.typography.body2
            )
            Text(
                text = "${chatInfo.lastMessageSender.username}:",
                modifier = Modifier.constrainAs(lastMessageSender) {
                    top.linkTo(lastMessageTime.bottom, margin = 10.dp)
                    start.linkTo(text.start)
                },
                style = MaterialTheme.typography.body1
            )
            Text(
                text = lastMsgText,
                modifier = Modifier.constrainAs(lastMessage) {
                    top.linkTo(lastMessageSender.bottom)
                    start.linkTo(text.start)
                    bottom.linkTo(parent.bottom, margin = 10.dp)
                },
                style = MaterialTheme.typography.body1
            )
        }
        Divider()
    }
}