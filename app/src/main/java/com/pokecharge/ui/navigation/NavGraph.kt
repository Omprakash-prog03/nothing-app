package com.pokecharge.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokecharge.ui.collection.CollectionScreen
import com.pokecharge.ui.detail.DetailScreen
import com.pokecharge.ui.home.HomeScreen
import com.pokecharge.ui.preview.PreviewScreen
import com.pokecharge.ui.settings.SettingsScreen
import com.pokecharge.ui.splash.SplashScreen
import com.pokecharge.ui.types.TypeSelectionScreen

private const val ANIM_MS = 260

@Composable
fun PokeChargeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeSlideIn() },
        exitTransition = { fadeSlideOut() },
        popEnterTransition = { fadeSlideIn(reverse = true) },
        popExitTransition = { fadeSlideOut(reverse = true) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenCollection = { navController.navigate(Routes.collection()) },
                onOpenTypes = { navController.navigate(Routes.TYPES) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenPreview = { dexId -> navController.navigate(Routes.preview(dexId)) },
            )
        }
        composable(
            route = Routes.COLLECTION,
            arguments = listOf(navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) { backStackEntry ->
            val typeFilter = backStackEntry.arguments?.getString("type")
            CollectionScreen(
                initialTypeFilter = typeFilter,
                onBack = { navController.popBackStack() },
                onOpenDetail = { dexId -> navController.navigate(Routes.detail(dexId)) },
            )
        }
        composable(Routes.TYPES) {
            TypeSelectionScreen(
                onBack = { navController.popBackStack() },
                onTypeSelected = { typeId -> navController.navigate(Routes.collection(typeId)) },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("dexId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val dexId = backStackEntry.arguments?.getInt("dexId") ?: return@composable
            DetailScreen(
                dexId = dexId,
                onBack = { navController.popBackStack() },
                onPreview = { navController.navigate(Routes.preview(dexId)) },
            )
        }
        composable(
            route = Routes.PREVIEW,
            arguments = listOf(navArgument("dexId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val dexId = backStackEntry.arguments?.getInt("dexId") ?: return@composable
            PreviewScreen(dexId = dexId, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun AnimatedContentTransitionScope<*>.fadeSlideIn(reverse: Boolean = false) =
    slideIntoContainer(
        towards = if (reverse) AnimatedContentTransitionScope.SlideDirection.Right else AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(ANIM_MS),
    )

private fun AnimatedContentTransitionScope<*>.fadeSlideOut(reverse: Boolean = false) =
    slideOutOfContainer(
        towards = if (reverse) AnimatedContentTransitionScope.SlideDirection.Right else AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(ANIM_MS),
    )
