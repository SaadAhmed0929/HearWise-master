package com.example.hearwise.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val VividRed = Color(0xFFFF0000)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@Composable
fun SettingsTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Account", fontSize = 18.sp, color = VividRed, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        ListItemStub("Manage Subscriptions")
        ListItemStub("Log Out")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Data & History", fontSize = 18.sp, color = VividRed, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        ListItemStub("View Transcription Logs")
        ListItemStub("Clear Local Data")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Appearance", fontSize = 18.sp, color = VividRed, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        ListItemStub("Dark / Light Mode (Active: Dark)")
    }
}

@Composable
private fun ListItemStub(text: String) {
    Text(
        text = text,
        color = White,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
    Divider(color = SurfaceGray, thickness = 1.dp)
}
