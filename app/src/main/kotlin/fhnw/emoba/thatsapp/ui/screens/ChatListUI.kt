package fhnw.emoba.thatsapp.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.data.ChatInfo
import fhnw.emoba.thatsapp.data.messages.*
import fhnw.emoba.thatsapp.data.toDateString
import fhnw.emoba.thatsapp.data.toTimeString
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.DefaultTopBar
import fhnw.emoba.thatsapp.ui.Drawer
import fhnw.emoba.thatsapp.ui.ImageView

@ExperimentalAnimationApi
@Composable
fun ChatListUI(model: ThatsAppModel, navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()

    with(model) {

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { DefaultTopBar(title = activeScreen.title, scaffoldState = scaffoldState) },
            drawerContent = { Drawer(model, navController) }
        ) {
            ChatListBody(model = model, navController = navController)
        }
    }
}

@Composable
private fun ChatListBody(model: ThatsAppModel, navController: NavController) {
    with(model) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                items(chatInfos.values.sortedBy { it.messages.sortedBy { it.sendTime } .last().sendTime } .reversed()) {
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
            val chatTitle =
                chatInfo.members.filter { it != model.ownUser }.joinToString(", ") { it.username }

            val lastMsg = chatInfo.messages.maxByOrNull { it.sendTime }!!

            val lastMsgText = when (lastMsg) {
                is MessageText -> lastMsg.data.text
                is MessageImage -> "hat ein Bild gesendet"
                is MessageCoordinates -> "hat Standortdaten geteilt"
                is SystemMessageNewChat -> "hat den Chat erstellt"
                is SystemMessageLeaveChat -> "hat den Chat verlassen"
                else -> ""
            }


            Box(modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 10.dp)
                    bottom.linkTo(parent.bottom, margin = 10.dp)
                    height = Dimension.fillToConstraints
                }
                .width(90.dp)
                .defaultMinSize(minWidth = 90.dp)) {
                ImageView(image = chatInfo.chatImage, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = chatTitle,
                modifier = Modifier.constrainAs(text) {
                    start.linkTo(image.end, margin = 10.dp)
                    top.linkTo(parent.top, margin = 10.dp)
                    end.linkTo(lastMessageDate.start, margin = 10.dp)
                    width = Dimension.fillToConstraints
                },
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = lastMsg.sendTime.toDateString(),
                modifier = Modifier.constrainAs(lastMessageDate) {
                    end.linkTo(parent.end, margin = 10.dp)
                    top.linkTo(parent.top, margin = 10.dp)
                },
                style = MaterialTheme.typography.body2
            )
            Text(
                text = lastMsg.sendTime.toTimeString(),
                modifier = Modifier.constrainAs(lastMessageTime) {
                    end.linkTo(parent.end, margin = 10.dp)
                    top.linkTo(lastMessageDate.bottom)
                },
                style = MaterialTheme.typography.body2
            )
            Text(
                text = "${userInfos[lastMsg.senderID.toString()]!!.username}:",
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
                    end.linkTo(parent.end, margin = 10.dp)
                    width = Dimension.fillToConstraints
                },
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Divider()
    }
}