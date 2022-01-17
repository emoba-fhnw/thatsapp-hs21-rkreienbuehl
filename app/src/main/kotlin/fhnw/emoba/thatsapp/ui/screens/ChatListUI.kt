package fhnw.emoba.thatsapp.ui.screens

import android.graphics.ImageDecoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.data.ChatInfo
import fhnw.emoba.thatsapp.data.UserInfo
import fhnw.emoba.thatsapp.data.messages.*
import fhnw.emoba.thatsapp.data.toDateString
import fhnw.emoba.thatsapp.data.toTimeString
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.Drawer
import fhnw.emoba.thatsapp.ui.ImageView
import fhnw.emoba.thatsapp.ui.MenuIcon

@ExperimentalAnimationApi
@Composable
fun ChatListUI(model: ThatsAppModel, navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()

    with(model) {

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { ChatListTopBar(model = model, title = activeScreen.title, scaffoldState = scaffoldState) },
            drawerContent = { Drawer(model, navController) }
        ) {
            ChatListBody(model = model, navController = navController)

            NewChatAlert(model = model, navController = navController)
        }
    }
}

@Composable
fun ChatListTopBar(model: ThatsAppModel, title: String, scaffoldState: ScaffoldState) {
    with(model) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { MenuIcon(scaffoldState) },
            actions = {
                IconButton(
                    onClick = { dialogOpen = !dialogOpen }
                ) {
                    Icon(Icons.Filled.AddCircle, "Neuer Chat")
                }
            }
        )
    }
}

@Composable
private fun ChatListBody(model: ThatsAppModel, navController: NavController) {
    with(model) {
        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(state = listState) {
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
            })
        ) {
            val (badge, image, text, lastMessageDate, lastMessageTime, lastMessageSender, lastMessage) = createRefs()
            val chatTitle =
                chatInfo.members.filter { it != model.ownUser }.joinToString(", ") { it.username }

            val imageBitmap = if (chatInfo.imageLoadingFailed && chatInfo.members.size == 2) {
                chatInfo.members.filter { it != ownUser }.first().userImage
            } else {
                chatInfo.chatImage
            }

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
                ImageView(image = imageBitmap, modifier = Modifier.fillMaxSize())
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

            if (chatInfo.unreadMessages() != 0) {
                Badge(modifier = Modifier.constrainAs(badge) {
                    top.linkTo(parent.top, margin = 10.dp)
                    start.linkTo(parent.start, margin = 90.dp)
                }) {
                    Text(text = "${chatInfo.unreadMessages()}")
                }
            }
        }
        Divider()
    }
}

@Composable
private fun NewChatAlert(model: ThatsAppModel, navController: NavHostController) {
    with(model) {
        val userList = rememberSaveable { mutableSetOf<String>() }

        if (dialogOpen) {

            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onCloseRequest.
                    dialogOpen = false
                    photo = null
                },
                title = {
                    Text(
                        text = "Neuer Chat",
                        style = MaterialTheme.typography.h6
                    )
                },
                text = {
                    NewChatContent(
                        model = model,
                        onChange = { checked, userID ->
                            if (checked) {
                                userList.add(userID)
                            } else {
                                userList.remove(userID)
                            }
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            createNewChat(userList.toList(), photo, onPublished = {
                                dialogOpen = false
                                photo = null
                                navController.navigate("chat/$it")
                            })

                        }
                    ) {
                        Text("Chat erstellen")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            dialogOpen = false
                            photo = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}

@Composable
private fun NewChatContent(model: ThatsAppModel, onChange: (Boolean, String) -> Unit) {
    with(model) {
        val context = LocalContext.current

        val selectImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                photo =  ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri!!))
                    .asImageBitmap()
            }
        }

        Column {
            Row {
                Box(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)) {
                    if (photo == null) {
                        ImageView(image = getDefaultImage(), modifier = Modifier.fillMaxSize())
                    } else {
                        ImageView(image = photo, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Button(onClick = { takePhoto() }) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "picture")
                        Text(text = "Bild aufnehmen")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(onClick = { selectImageLauncher.launch("image/*") }) {
                        Icon(Icons.Filled.Image, contentDescription = "picture")
                        Text(text = "Bild auswählen")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier
                .border(width = 1.dp, color = Color.Black)
                .aspectRatio(1f)
            ) {
                LazyColumn {
                    items(
                        items = userInfos.values.filter { it != ownUser }
                    ) {
                        UserRow(
                            userInfo = it,
                            onChange = onChange)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(userInfo: UserInfo, onChange: (Boolean, String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        var checked by remember { mutableStateOf(false) }
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = !checked
                onChange.invoke(checked, userInfo.id.toString())
            })

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = userInfo.username,
            style = MaterialTheme.typography.h6
        )
    }
    Divider()
}