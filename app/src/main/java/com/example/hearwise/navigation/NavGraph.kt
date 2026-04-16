package com.example.hearwise.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.hearwise.ui.screens.main.MainScreen
import com.example.hearwise.ui.screens.calibration.CalibrationScreen
import com.example.hearwise.ui.screens.another.AnotherScreen
import com.example.hearwise.ui.screens.auth.LoginScreen
import com.example.hearwise.ui.screens.auth.SignupScreen
import com.example.hearwise.ui.screens.onboarding.OnboardingScreen
import com.example.hearwise.ui.screens.onboarding.HearingTestPromptScreen
import androidx.navigation.navArgument

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("HearWisePrefs", android.content.Context.MODE_PRIVATE)
    val isOnboardingComplete = prefs.getBoolean("onboarding_complete", false)

    NavHost(
        navController = navController,
        startDestination = if (isOnboardingComplete) "login" else "onboarding"
    ) {

        composable(
            route = "main?micDenied={micDenied}",
            arguments = listOf(navArgument("micDenied") { defaultValue = "false" })
        ) { backStackEntry ->
            val micDenied = backStackEntry.arguments?.getString("micDenied")?.toBoolean() ?: false
            MainScreen(
                micDenied = micDenied,
                onRetakeTest = {
                    navController.navigate("calibration")
                }
            )
        }

        composable("calibration") {
            CalibrationScreen(
                onCalibrationComplete = {
                    navController.navigate("main?micDenied=false") {
                        popUpTo("calibration") { inclusive = true }
                    }
                }
            )
        }

        composable("another_screen") {
            AnotherScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginClick = { _, _ ->
                    navController.navigate("hearing_test_prompt") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupClick = {
                    navController.navigate("hearing_test_prompt") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("hearing_test_prompt") {
            HearingTestPromptScreen(
                onComplete = { micDenied ->
                    if (micDenied) {
                        navController.navigate("main?micDenied=true") {
                            popUpTo("hearing_test_prompt") { inclusive = true }
                        }
                    } else {
                        navController.navigate("calibration") {
                            popUpTo("hearing_test_prompt") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
