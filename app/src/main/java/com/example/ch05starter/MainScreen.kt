package com.example.ch05starter

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.navDeepLink


//        Test dengan:
//        adb shell am start -W -a android.intent.action.VIEW \
//            -d "myapp://article/3" com.example.ch05starter
// ============================================================================

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick  = {
                            navController.navigate(item.route) {
                                popUpTo(Routes.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = {
                            Icon(
                                imageVector =
                                    if (currentRoute == item.route)
                                        item.iconSelected
                                    else
                                        item.iconUnselected,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {

            composable(
                route           = Routes.Home.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                exitTransition  = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }
            ) {
                HomeScreen(navController)
            }

            composable(
            route           = Routes.Explore.route,
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition  = { slideOutHorizontally { -it } + fadeOut() }
            ) {
                ExploreScreen()
            }

            composable(
                route           = Routes.Profile.route,
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition  = { slideOutHorizontally { -it } + fadeOut() }
            ) {
                ProfileScreen()
            }


            composable(
                route     = Routes.Detail.route,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.IntType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "myapp://article/{itemId}" }
                ),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition  = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                DetailScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
