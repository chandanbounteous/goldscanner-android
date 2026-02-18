package com.kanishk.goldscanner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.kanishk.goldscanner.presentation.ui.screen.*
import com.kanishk.goldscanner.presentation.ui.screen.CustomerScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate("main/0") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("login") {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate("main/0") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            "main/{selectedTab}",
            arguments = listOf(navArgument("selectedTab") { 
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val selectedTab = backStackEntry.arguments?.getInt("selectedTab") ?: 0
            MainScreen(
                initialSelectedTab = selectedTab,
                onNavigateToArticleDetail = {
                    navController.navigate("article_detail")
                },
                onNavigateToCustomers = {
                    navController.navigate("customers")
                }
            )
        }
        
        composable("article_detail") {
            ArticleDetailScreen(
                onNavigateBack = {
                    navController.navigate("main/1") {
                        popUpTo("article_detail") { inclusive = true }
                    }
                },
                onNavigateToBasket = {
                    navController.navigate("main/3") {
                        popUpTo("article_detail") { inclusive = true }
                    }
                }
            )
        }
        
        composable("customers") {
            CustomerScreen()
        }
    }
}