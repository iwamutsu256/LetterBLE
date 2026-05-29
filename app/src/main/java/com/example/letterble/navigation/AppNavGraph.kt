/**
 * AppNavGraph.kt
 *
 * 役割:
 * - 全画面の遷移ルートを定義する
 * - NavHostを定義する
 * - 画面間のルーティングを管理する
 *
 * 注意:
 * - ビジネスロジックを書かない
 * - ViewModelの代わりに状態管理をしない
 */
package com.example.letterble.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.letterble.di.AppContainer
import com.example.letterble.feature.carry.CarryDetailScreen
import com.example.letterble.feature.carry.CarryScreen
import com.example.letterble.feature.edit_letter.EditLetterScreen
import com.example.letterble.feature.edit_letter.PostSelectScreen
import com.example.letterble.feature.home.HomeScreen
import com.example.letterble.feature.received.ReceivedDetailScreen
import com.example.letterble.feature.received.ReceivedScreen
import com.example.letterble.feature.register.RegisterScreen
import com.example.letterble.service.BleForegroundService
/**
 * アプリ全体の画面遷移を定義する。
 *
 * @param navController 画面遷移を実行するNavController
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val context = LocalContext.current
    val startDestination = if (
        appContainer.userRepository.getCurrentUserName().isNullOrBlank()
    ) {
        Destinations.REGISTER
    } else {
        Destinations.HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.REGISTER) {
            RegisterScreen(
                appContainer = appContainer,
                onRegistered = {
                    BleForegroundService.start(context)
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.HOME) {
            HomeScreen(
                navController = navController,
                appContainer = appContainer,
                onReceivedClicked = { navController.navigate(Destinations.RECEIVED) },
                onCarryClicked = { navController.navigate(Destinations.CARRY) },
                onCreateLetterClicked = { navController.navigate(Destinations.EDIT_LETTER) }
            )
        }

        composable(Destinations.EDIT_LETTER) {
            EditLetterScreen(
                appContainer = appContainer,
                onBackClicked = navController::popBackStack,
                onSubmitClicked = {
                    navController.navigate(Destinations.POST_SELECT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destinations.POST_SELECT) {
            PostSelectScreen(
                appContainer = appContainer,
                onBackClicked = navController::popBackStack,
                onSubmitted = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destinations.RECEIVED) {
            ReceivedScreen(
                appContainer = appContainer,
                onLetterClicked = { letterId ->
                    navController.navigate(Destinations.receivedDetail(letterId))
                },
                onBackClicked = navController::popBackStack
            )
        }

        composable(
            route = Destinations.RECEIVED_DETAIL,
            arguments = listOf(navArgument(Destinations.LETTER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            ReceivedDetailScreen(
                appContainer = appContainer,
                letterId = backStackEntry.arguments?.getString(Destinations.LETTER_ID_ARG).orEmpty(),
                onBackClicked = navController::popBackStack
            )
        }

        composable(Destinations.CARRY) {
            CarryScreen(
                appContainer = appContainer,
                onLetterClicked = { letterId ->
                    navController.navigate(Destinations.carryDetail(letterId))
                },
                onBackClicked = navController::popBackStack
            )
        }

        composable(
            route = Destinations.CARRY_DETAIL,
            arguments = listOf(navArgument(Destinations.LETTER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            CarryDetailScreen(
                appContainer = appContainer,
                letterId = backStackEntry.arguments?.getString(Destinations.LETTER_ID_ARG).orEmpty(),
                onBackClicked = navController::popBackStack
            )
        }
    }
}
