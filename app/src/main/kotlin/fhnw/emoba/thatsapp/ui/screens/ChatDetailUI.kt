package fhnw.emoba.thatsapp.ui.screens

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import fhnw.emoba.thatsapp.ui.ImageView

@ExperimentalComposeUiApi
@Composable
fun ChatDetailUI(model: ThatsAppModel, navController: NavHostController, chatID: String?) {

    with(model) {
        var chatInfo: ChatInfo? = null
        if (chatID != null) { chatInfo = chatInfos[chatID] }

        var chatTitle = "Unbekannter Chat"
        if (chatInfo != null) {
            chatTitle =
                chatInfo.members.filter { it != model.ownUser }.joinToString(", ") { it.username }
        }

        Scaffold(
            topBar = { ChatTopBar(model, chatTitle, navController) }
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
        navController.navigateUp()
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
            .padding(bottom = 55.dp)) {
            LazyColumn {
                items(chatInfo.messages.sortedBy { it.sendTime }) {
                    ChatMessageRow(model, it)
                }
            }
            // Spacer(modifier = Modifier.fillMaxHeight())
        }

        BottomAppBar(modifier = Modifier.constrainAs(newMsg) {
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }) {
            NewMessage(model, chatInfo)
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun NewMessage(model: ThatsAppModel, chatInfo: ChatInfo) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (inputField, sendButton, imageButton, positionButton) = createRefs()
        var message by remember { mutableStateOf("") }

        val keyboard = LocalSoftwareKeyboardController.current

        OutlinedTextField(value = message, onValueChange = {
            message = it
        }, modifier = Modifier.constrainAs(inputField) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(sendButton.start)
        },
            keyboardOptions = KeyboardOptions(imeAction    = ImeAction.Done,
                autoCorrect  = false,
                keyboardType = KeyboardType.Ascii),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()
            })
        )

        IconButton(onClick = {
            model.sendTextMessage(message, chatInfo)
            message = ""
        }, modifier = Modifier.constrainAs(sendButton) {
            end.linkTo(imageButton.start)
        }) {
            Icon(Icons.Filled.Send, contentDescription = "send")
        }

        PictureButton(model = model, modifier = Modifier.constrainAs(imageButton) {
            end.linkTo(positionButton.start)
        })

        IconButton(onClick = {
            model.sendPositionMessage(chatInfo)
        }, modifier = Modifier.constrainAs(positionButton) {
            end.linkTo(parent.end)
        }) {
            Icon(Icons.Filled.LocationOn, contentDescription = "send")
        }
    }
}

@Composable
private fun PictureButton(model: ThatsAppModel, modifier: Modifier) {
    with(model) {
        val openDialog = rememberSaveable { mutableStateOf(false) }

        IconButton(
            onClick = {
                takePhoto()
            },
            modifier = modifier) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "picture")
            }

        if (photoDialogOpen) {

            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onCloseRequest.
                    photoDialogOpen = false
                },
                title = {
                    Text(
                        text = "Foto senden",
                        style = MaterialTheme.typography.h6
                    )
                },
                text = {
                    ImageView(image = photo, modifier = Modifier.fillMaxWidth())
                },
                confirmButton = {
                    Button(
                        onClick = {
                            photoDialogOpen = false
                        }
                    ) {
                        Text("Senden")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            photoDialogOpen = false
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
        is SystemMessageNewChat -> "$username hat neuen Chat erstellt"
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
        var bgColor = Color.Cyan

        if (message.senderID == ownUser.id) {
            arrangement = Arrangement.End
            bgColor = Color.Green
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