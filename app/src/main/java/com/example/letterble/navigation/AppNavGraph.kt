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
import com.example.letterble.feature.received.ReceivedMapDetailScreen
import com.example.letterble.feature.received.ReceivedScreen
import com.example.letterble.feature.register.RegisterScreen
import com.example.letterble.service.BleForegroundService
import com.example.letterble.service.BlePrerequisiteReport
/**
 * アプリ全体の画面遷移を定義する。
 *
 * @param navController 画面遷移を実行するNavController
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    blePermissionErrorMessage: String? = null,
    bleSetupReport: BlePrerequisiteReport? = null,
    onOpenAppSettingsClicked: () -> Unit = {},
    onOpenBluetoothSettingsClicked: () -> Unit = {},
    onOpenLocationSettingsClicked: () -> Unit = {},
    onRequestBlePermissionsClicked: () -> Unit = {},
    onDismissBleSetupClicked: () -> Unit = {},
    onRequestAddBleTileClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val navigateBackOrHome = {
        if (!navController.popBackStack()) {
            navController.navigate(Destinations.HOME) {
                launchSingleTop = true
            }
        }
    }
    val navigateHome = {
        if (!navController.popBackStack(Destinations.HOME, inclusive = false)) {
            navController.navigate(Destinations.HOME) {
                launchSingleTop = true
            }
        }
    }
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
                    BleForegroundService.startIfReady(
                        context = context,
                        userName = appContainer.userRepository.getCurrentUserName()
                    )
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
                blePermissionErrorMessage = blePermissionErrorMessage,
                bleSetupReport = bleSetupReport,
                onOpenAppSettingsClicked = onOpenAppSettingsClicked,
                onOpenBluetoothSettingsClicked = onOpenBluetoothSettingsClicked,
                onOpenLocationSettingsClicked = onOpenLocationSettingsClicked,
                onRequestBlePermissionsClicked = onRequestBlePermissionsClicked,
                onDismissBleSetupClicked = onDismissBleSetupClicked,
                onRequestAddBleTileClicked = onRequestAddBleTileClicked,
                onReceivedClicked = {
                    navController.navigate(Destinations.RECEIVED) {
                        launchSingleTop = true
                    }
                },
                onCarryClicked = {
                    navController.navigate(Destinations.CARRY) {
                        launchSingleTop = true
                    }
                },
                onCreateLetterClicked = {
                    navController.navigate(Destinations.EDIT_LETTER) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destinations.EDIT_LETTER) {
            EditLetterScreen(
                appContainer = appContainer,
                onBackClicked = navigateBackOrHome,
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
                onBackClicked = navigateBackOrHome,
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
                navController = navController,
                appContainer = appContainer,
                onLetterClicked = { letterId ->
                    navController.navigate(Destinations.receivedDetail(letterId)) {
                        launchSingleTop = true
                    }
                },
                onBackClicked = navigateHome
            )
        }

        composable(
            route = Destinations.RECEIVED_DETAIL,
            arguments = listOf(navArgument(Destinations.LETTER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val letterId = backStackEntry.arguments?.getString(Destinations.LETTER_ID_ARG).orEmpty()
            ReceivedDetailScreen(
                appContainer = appContainer,
                letterId = letterId,
                onBackClicked = navigateBackOrHome,
                onMapClicked = {
                    navController.navigate(Destinations.receivedMapDetail(letterId))
                }
            )
        }

        composable(
            route = Destinations.RECEIVED_MAP_DETAIL,
            arguments = listOf(navArgument(Destinations.LETTER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            ReceivedMapDetailScreen(
                appContainer = appContainer,
                letterId = backStackEntry.arguments?.getString(Destinations.LETTER_ID_ARG).orEmpty(),
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(Destinations.CARRY) {
            CarryScreen(
                navController = navController,
                appContainer = appContainer,
                onLetterClicked = { letterId ->
                    navController.navigate(Destinations.carryDetail(letterId)) {
                        launchSingleTop = true
                    }
                },
                onBackClicked = navigateHome
            )
        }

        composable(
            route = Destinations.CARRY_DETAIL,
            arguments = listOf(navArgument(Destinations.LETTER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            CarryDetailScreen(
                appContainer = appContainer,
                letterId = backStackEntry.arguments?.getString(Destinations.LETTER_ID_ARG).orEmpty(),
                onBackClicked = navigateBackOrHome
            )
        }
    }
}
