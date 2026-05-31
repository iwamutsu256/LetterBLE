package com.example.letterble.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.letterble.navigation.Destinations
import com.example.letterble.ui.theme.LetterBLEColors
import com.example.letterble.ui.theme.LetterBLESizes
import com.example.letterble.ui.theme.LetterBLESpacing

/**
 * ボトムナビゲーションを内包した共通のScaffoldコンポーネント。
 * 各画面のルートとしてこれを使用することで、ナビゲーションの実装を簡略化できます。
 *
 * 【使い方】
 * CommonBottomNavigation(navController = navController) { innerPadding ->
 *     // 画面のメインコンテンツ
 *     Box(modifier = Modifier.padding(innerPadding)) { ... }
 * }
 *
 * @param navController ナビゲーション制御用のNavController
 * @param modifier 修飾子
 * @param content 画面のメインコンテンツ。PaddingValuesを受け取り、コンテンツの余白として適用してください。
 */
@Composable
fun CommonBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = LetterBLEColors.AppBackground,
        bottomBar = {
            CommonBottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // ホーム画面をスタックの基点にし、画面の重複を防ぐ設定
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

/**
 * ボトムナビゲーションの項目を定義する列挙型
 */
enum class NavigationDestination(val route: String, val label: String, val icon: ImageVector) {
    RECEIVED(Destinations.RECEIVED, "受信", Icons.Default.Email),
    HOME(Destinations.HOME, "ホーム", Icons.Default.Home),
    CARRY(Destinations.CARRY, "配達", Icons.Default.Place)
}

/**
 * ボトムナビゲーションバー自体のUI実装（内部用）
 */
@Composable
private fun CommonBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = LetterBLESpacing.Large,
                end = LetterBLESpacing.Large,
                top = LetterBLESpacing.Small,
                bottom = LetterBLESpacing.Small
            )
            .fillMaxWidth(),
        color = LetterBLEColors.NavigationContainer,
        shape = RoundedCornerShape(LetterBLESizes.BottomNavigationCorner)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LetterBLESizes.BottomNavigationHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = NavigationDestination.entries
            items.forEachIndexed { index, destination ->
                val isSelected = currentRoute == destination.route
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (!isSelected) {
                                onNavigate(destination.route)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                LetterBLEColors.NavigationContent
                            } else {
                                LetterBLEColors.NavigationContent.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(LetterBLESizes.BottomNavigationIcon)
                        )
                        Spacer(modifier = Modifier.height(LetterBLESpacing.Tiny))
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) {
                                LetterBLEColors.NavigationContent
                            } else {
                                LetterBLEColors.NavigationContent.copy(alpha = 0.6f)
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(LetterBLESizes.BottomNavigationDividerWidth)
                            .height(LetterBLESizes.BottomNavigationDividerHeight)
                            .background(LetterBLEColors.NavigationDivider.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommonBottomNavigationPreview() {
    // プレビュー用にバー単体を表示
    CommonBottomNavigationBar(
        currentRoute = Destinations.HOME,
        onNavigate = {}
    )
}
