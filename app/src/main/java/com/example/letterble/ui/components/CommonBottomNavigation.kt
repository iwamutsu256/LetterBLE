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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.letterble.navigation.Destinations

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
        containerColor = Color(0xFFFFFFFA), // HomeScreenの背景色と合わせる
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
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        color = Color(0xFF000066), // 濃い青色
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
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
                            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = destination.label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color.White.copy(alpha = 0.5f))
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
