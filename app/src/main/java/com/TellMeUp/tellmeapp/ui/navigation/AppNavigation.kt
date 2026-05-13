/**
 * @file: AppNavigation.kt
 * @description: Bottom navigation and Compose Navigation setup
 * @dependencies: Compose Navigation, MainScreen, SubscriptionScreen, SettingsScreen
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.TellMeUp.tellmeapp.ui.screen.aiprovider.AiProviderScreen
import com.TellMeUp.tellmeapp.ui.screen.logs.LogsScreen
import com.TellMeUp.tellmeapp.ui.screen.main.MainScreen
import com.TellMeUp.tellmeapp.ui.screen.settings.SettingsScreen
import com.TellMeUp.tellmeapp.ui.screen.subscription.SubscriptionScreen
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.BackgroundDark
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Main : Screen("main", "Голос", Icons.Filled.Home)
    data object Subscription : Screen("subscription", "Подписка", Icons.Filled.Star)
    data object Logs : Screen("logs", "Логи", Icons.Filled.List)
    data object Settings : Screen("settings", "Настройки", Icons.Filled.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Main, Screen.Subscription, Screen.Logs, Screen.Settings)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomBar = currentRoute != "ai_provider/{providerKey}"

            if (showBottomBar) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                        AccentBlue else TextTertiary
                                )
                            },
                            label = {
                                Text(
                                    screen.label,
                                    fontSize = 11.sp,
                                    color = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                        AccentBlue else TextTertiary
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = AccentBlue.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    onNavigateToProvider = { provider ->
                        navController.navigate("ai_provider/${provider.key}")
                    }
                )
            }
            composable(Screen.Subscription.route) {
                SubscriptionScreen()
            }
            composable(Screen.Logs.route) {
                LogsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = "ai_provider/{providerKey}",
                arguments = listOf(navArgument("providerKey") { type = NavType.StringType })
            ) {
                AiProviderScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
