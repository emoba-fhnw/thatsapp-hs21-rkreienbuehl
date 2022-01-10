package fhnw.emoba.thatsapp.ui


import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fhnw.emoba.thatsapp.data.Screens
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.thatsapp.ui.screens.*


@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun AppUI(model : ThatsAppModel) {
    val navController = rememberAnimatedNavController()

    MaterialTheme {
        AnimatedNavHost(navController, startDestination = "chatList") {
            composable(
                "chatList",
                enterTransition = {
                    when (initialState.destination.route) {
                        "chat/{chatID}" ->
                            slideIntoContainer(
                                AnimatedContentScope.SlideDirection.Right,
                                animationSpec = tween(700)
                            )
                        else -> fadeIn(animationSpec = tween(700))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        "chat/{chatID}" ->
                            slideOutOfContainer(
                                AnimatedContentScope.SlideDirection.Left,
                                animationSpec = tween(700)
                            )
                        else -> fadeOut(animationSpec = tween(700))
                    }
                }
            ) { ChatListUI(model, navController) }
            composable(
                "chat/{chatID}",
                enterTransition = {
                    when (initialState.destination.route) {
                        "chatList" ->
                            slideIntoContainer(
                                AnimatedContentScope.SlideDirection.Left,
                                animationSpec = tween(700)
                            )
                        else -> fadeIn(animationSpec = tween(700))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        "chatList" ->
                            slideOutOfContainer(
                                AnimatedContentScope.SlideDirection.Right,
                                animationSpec = tween(700)
                            )
                        else -> fadeOut(animationSpec = tween(700))
                    }
                }
            ) { backStackEntry ->
                ChatDetailUI(
                    model,
                    navController,
                    backStackEntry.arguments?.getString("chatID")
                )
            }
            composable(
                "users",
                enterTransition = {
                    fadeIn(animationSpec = tween(700))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(700))
                }
            ) {
                UserListUI(model, navController)
            }
            composable(
                "settings",
                enterTransition = {
                    fadeIn(animationSpec = tween(700))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(700))
                }
            ) {
                SettingsUI(model, navController)
            }
        }
    }
}
