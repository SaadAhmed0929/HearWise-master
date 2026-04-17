package com.example.hearwise.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.hearwise.feature.dashboard.MainScreen
import com.example.hearwise.feature.calibration.CalibrationScreen
import com.example.hearwise.feature.another.AnotherScreen
import com.example.hearwise.feature.auth.LoginScreen
import com.example.hearwise.feature.auth.SignupScreen
import com.example.hearwise.feature.onboarding.OnboardingScreen
import com.example.hearwise.feature.onboarding.HearingTestPromptScreen
import com.example.hearwise.feature.onboarding.SplashScreen
import androidx.navigation.navArgument
import com.example.hearwise.data.model.ProfileManager

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("HearWisePrefs", android.content.Context.MODE_PRIVATE)
    val isOnboardingComplete = prefs.getBoolean("onboarding_complete", false)

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    val destination = if (isOnboardingComplete) "login" else "onboarding"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

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
                    val hasProfile = ProfileManager.loadProfile(context) != null
                    val destination = if (hasProfile) "main?micDenied=false" else "hearing_test_prompt"
                    navController.navigate(destination) {
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
                    val hasProfile = ProfileManager.loadProfile(context) != null
                    val destination = if (hasProfile) "main?micDenied=false" else "hearing_test_prompt"
                    navController.navigate(destination) {
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
