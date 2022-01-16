package fhnw.emoba.thatsapp.ui.screens

import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.decodeBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.data.ChatInfo
import fhnw.emoba.thatsapp.data.messages.*
import fhnw.emoba.thatsapp.data.toDateString
import fhnw.emoba.thatsapp.data.toTimeString
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.ImageAlert
import fhnw.emoba.thatsapp.ui.ImageView

@ExperimentalComposeUiApi
@Composable
fun ChatDetailUI(model: ThatsAppModel, navController: NavHostController, chatID: String?) {

    with(model) {
        var chatInfo = chatInfos[chatID]!!

        var chatTitle = "Unbekannter Chat"
        if (chatInfo != null) {
            chatTitle =
                chatInfo.members.filter { it != model.ownUser }.joinToString(", ") { it.username }
        }

        Scaffold(
            topBar = { ChatTopBar(model, chatTitle, navController) },
            bottomBar = { NewMessage(model, chatInfo) }
        ) {
            if (chatInfo == null) {
                Text(text = "Chat wurde nicht gefunden")
            } else {
                ChatMessageList(model = model, chatInfo = chatInfo)
            }
        }
    }
}

@Composable
private fun ChatTopBar(model: ThatsAppModel, title: String, navController: NavHostController) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { ChatMenuIcon(model, navController) }
    )
}

@Composable
private fun ChatMenuIcon(model: ThatsAppModel, navController: NavHostController) {
    IconButton(onClick = {
        navController.navigate("chatList")
    }) {
        Icon(Icons.Filled.ArrowBack, "Zurück")
    }
}

@ExperimentalComposeUiApi
@Composable
private fun ChatMessageList(model: ThatsAppModel, chatInfo: ChatInfo) {
    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .padding(
            top = 0.dp
        ))  {
        val (msgList, newMsg) = createRefs()

        Box(modifier = Modifier
            .constrainAs(msgList) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            }
            .padding(bottom = 65.dp)) {
            LazyColumn {
                items(chatInfo.messages.sortedBy { it.sendTime }) {
                    ChatMessageRow(model, it)
                }
            }
        }

    }
}

@ExperimentalComposeUiApi
@Composable
private fun NewMessage(model: ThatsAppModel, chatInfo: ChatInfo) {
    with(model) {
        var message by remember { mutableStateOf("") }
        val context = LocalContext.current

        val selectImageLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
            if (uri != null) {
                photo =  decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri!!)).asImageBitmap()
                dialogOpen = true
            }
        }

        val focusManager = LocalFocusManager.current

        OutlinedTextField(
            value = message,
            onValueChange = {
                message = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 5.dp,
                    start = 5.dp,
                    end = 5.dp
                ),
            trailingIcon = {
                Row {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        model.sendTextMessage(message, chatInfo)
                        message = ""
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "send")
                    }

                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            takePhoto()
                        }) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "picture")
                    }

                    IconButton(onClick = {
                        focusManager.clearFocus()
                        selectImageLauncher.launch("image/*")
                    }) {
                        Icon(Icons.Filled.Image, contentDescription = "image")
                    }

                    IconButton(onClick = {
                        focusManager.clearFocus()
                        model.sendPositionMessage(chatInfo)
                    }) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "position")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction    = ImeAction.Done,
                autoCorrect  = false,
                keyboardType = KeyboardType.Ascii),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    model.sendTextMessage(message, chatInfo)
                    message = ""
                })
        )

        ImageAlert(
            dialogOpen = dialogOpen,
            photo = photo,
            onConfirm = {
                dialogOpen = false
                uploadAndSendImage(chatInfo)
            },
            onDismiss = {
                dialogOpen = false
                photo = null
            }
        )
    }
}


@Composable
private fun ChatMessageRow(model: ThatsAppModel, message: Message) {
    when (message) {
        is SystemMessageNewChat,
        is SystemMessageLeaveChat -> SystemMessageRow(model, message)
        is MessageText,
        is MessageCoordinates,
        is MessageImage -> MessageRow(model, message)
    }
}

@Composable
private fun SystemMessageRow(model: ThatsAppModel, message: Message) {
    val user = model.userInfos[message.senderID.toString()]
    val username = user?.username ?: "Unbekannt"

    val text = when (message) {
        is SystemMessageNewChat -> "$username hat den Chat erstellt"
        is SystemMessageLeaveChat -> "$username hat den Chat verlassen"
        else -> ""
    }

    Spacer(modifier = Modifier.height(5.dp))
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(color = Color.LightGray), horizontalArrangement = Arrangement.Center) {
        Text(text = text, style = MaterialTheme.typography.body1)
    }
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
private fun MessageRow(model: ThatsAppModel, message: Message) {
    with(model) {
        val user = model.userInfos[message.senderID.toString()]
        val username = user?.username ?: "Unbekannt"

        var arrangement = Arrangement.Start
        var bgColor = MaterialTheme.colors.background

        if (message.senderID == ownUser.id) {
            arrangement = Arrangement.End
            bgColor = MaterialTheme.colors.secondary
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp), horizontalArrangement = arrangement) {
            Row(modifier = Modifier.fillMaxWidth(0.85f), horizontalArrangement = arrangement) {
                Card(elevation = 5.dp, backgroundColor = bgColor) {
                    ConstraintLayout {
                        val (sender, msgBox, msgTime) = createRefs()

                        Text(
                            "$username:",
                            modifier = Modifier.constrainAs(sender) {
                                top.linkTo(parent.top, margin = 5.dp)
                                start.linkTo(parent.start, margin = 5.dp)
                            },
                            style = MaterialTheme.typography.subtitle2,
                        )
                        Box(modifier = Modifier.constrainAs(msgBox) {
                            top.linkTo(sender.bottom, margin = 5.dp)
                            start.linkTo(sender.start, margin = 5.dp)
                            end.linkTo(msgTime.end, margin = 5.dp)
                            bottom.linkTo(msgTime.top, margin = 5.dp)
                        }) {
                            when (message) {
                                is MessageText -> Text(text = message.data.text, style = MaterialTheme.typography.body1)
                                is MessageCoordinates -> Coordinates(message)
                                is MessageImage -> ImageView(image = message.data.image, Modifier.fillMaxSize())
                            }
                        }
                        Text(
                            "${message.sendTime.toDateString()}, ${message.sendTime.toTimeString()}",
                            modifier = Modifier
                                .constrainAs(msgTime) {
                                    end.linkTo(parent.end, margin = 5.dp)
                                    bottom.linkTo(parent.bottom, margin = 5.dp)
                                }
                                .padding(start = 5.dp),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Coordinates(message: MessageCoordinates) {
    val uriHandler = LocalUriHandler.current

    Row {
        Icon(Icons.Filled.LocationOn, "")
        Text(text = message.data.geoCoordinates.dms(), modifier = Modifier.clickable {
            uriHandler.openUri(message.data.geoCoordinates.asGoogleMapsURL())
        })
    }
}