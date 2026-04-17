package com.example.hearwise.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.hearwise.feature.dashboard.HomeTabScreen
import com.example.hearwise.feature.dashboard.CaptionsTabScreen
import com.example.hearwise.feature.dashboard.ProfileTabScreen
import com.example.hearwise.feature.dashboard.SettingsTabScreen

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val VividRed = Color(0xFFFF0000)
private val Gray400 = Color(0xFF888888)

enum class MainTab(val title: String) {
    HOME("Amplifier"),
    CAPTIONS("Captions"),
    PROFILE("Profile"),
    SETTINGS("Settings")
}

@Composable
fun MainScreen(
    micDenied: Boolean = false,
    onRetakeTest: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceGray,
                contentColor = VividRed
            ) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { 
                            // Dummy icons, just text for prototype UI 
                            Text(tab.title.first().toString(), fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VividRed,
                            unselectedIconColor = Gray400,
                            selectedTextColor = VividRed,
                            unselectedTextColor = Gray400,
                            indicatorColor = PureBlack
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedContent(targetState = selectedTab, label = "tab_switch") { tab ->
                when (tab) {
                    MainTab.HOME -> HomeTabScreen(micDenied)
                    MainTab.CAPTIONS -> CaptionsTabScreen()
                    MainTab.PROFILE -> ProfileTabScreen(onRetakeTest = onRetakeTest)
                    MainTab.SETTINGS -> SettingsTabScreen()
                }
            }
        }
    }
}
