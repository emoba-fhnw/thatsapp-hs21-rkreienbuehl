package fhnw.emoba.thatsapp.ui.screens

import android.graphics.ImageDecoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.Drawer
import fhnw.emoba.thatsapp.ui.ImageAlert
import fhnw.emoba.thatsapp.ui.ImageView
import fhnw.emoba.thatsapp.ui.MenuIcon

@Composable
fun SettingsUI(model: ThatsAppModel, navController: NavHostController) {
    with(model) {
        val scaffoldState = rememberScaffoldState()
        val focusManager = LocalFocusManager.current

        var usernameChanged by rememberSaveable { mutableStateOf(false) }
        var profileImageChanged by remember { mutableStateOf(false) }

        var username by rememberSaveable { mutableStateOf(ownUser.username) }
        var profileImage by remember { mutableStateOf(ownUser.userImage) }

        with(model) {
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = { SettingsTopBar(
                    title = "Einstellungen",
                    scaffoldState = scaffoldState,
                    hasChanges = (usernameChanged || profileImageChanged),
                    onSave = {
                        focusManager.clearFocus()

                        if (usernameChanged) {
                            changeUsername(username)
                        }

                        if (profileImageChanged && profileImage != null) {
                            changeProfileImage(profileImage!!)
                        }

                        usernameChanged = false
                        profileImageChanged = false
                    }
                ) },
                drawerContent = { Drawer(model, navController) }
            ) {
                SettingsBody(
                    model = model,
                    username = username,
                    profileImage = profileImage,
                    onUsernameChange = {
                        usernameChanged = true
                        username = it
                    },
                    onProfileImageChanged = {
                        profileImageChanged = true
                        profileImage = photo
                        photo = null
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsTopBar(title: String, scaffoldState: ScaffoldState, hasChanges: Boolean, onSave: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { MenuIcon(scaffoldState) },
        actions = {
            IconButton(
                onClick = { onSave.invoke() },
                enabled = hasChanges
            ) {
                Icon(Icons.Filled.CheckCircle, "Speichern")
            }
        }
    )
}

@Composable
private fun SettingsBody(model: ThatsAppModel, username: String, profileImage: ImageBitmap?, onUsernameChange: (String) -> Unit, onProfileImageChanged: () -> Unit) {
    with(model) {
        val context = LocalContext.current

        val selectImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                photo =  ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri!!))
                    .asImageBitmap()
                dialogOpen = true
            }
        }

        val focusManager = LocalFocusManager.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = {
                    onUsernameChange(it)
                } ,
                label = { Text("Username") },
                placeholder = { Text(text = "Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction    = ImeAction.Done,
                    autoCorrect  = false,
                    keyboardType = KeyboardType.Ascii),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
            )

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                ImageView(image = profileImage, modifier = Modifier.fillMaxWidth())
            }

            Row(modifier = Modifier.padding(5.dp)) {
                Button(onClick = { takePhoto() }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "picture")
                    Text(text = "Bild aufnehmen")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(onClick = { selectImageLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.Image, contentDescription = "picture")
                    Text(text = "Bild auswählen")
                }
            }


            ImageAlert(
                dialogOpen = dialogOpen,
                photo = photo,
                onConfirm = {
                    dialogOpen = false
                    onProfileImageChanged()
                },
                onDismiss = {
                    dialogOpen = false
                    photo = null
                }
            )
        }
    }
}