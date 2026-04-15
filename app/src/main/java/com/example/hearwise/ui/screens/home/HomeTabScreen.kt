package com.example.hearwise.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val SurfaceLight = Color(0xFF1E1E1E)
private val VividRed = Color(0xFFFF0000)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@Composable
fun HomeTabScreen(micDenied: Boolean = false) {
    var isAmplifierActive by remember { mutableStateOf(false) }
    var masterVolume by remember { mutableFloatStateOf(0.5f) }
    var selectedPreset by remember { mutableStateOf("Normal") }
    val presets = listOf("Normal", "Restaurant", "TV")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner layout if Mic is Denied
        if (micDenied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Mic permission required to use amplifier.", color = White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top Pill: Device Selector Native Mock
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .background(SurfaceGray, RoundedCornerShape(24.dp))
                .clickable { /* Opens device selector bottom sheet */ }
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VividRed))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bluetooth Earbuds", color = White, fontWeight = FontWeight.Medium)
        }
        Text("High Latency Warning", color = Color(0xFFFFA000), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.weight(1f))

        // Power Toggle
        val btnColor = if (isAmplifierActive) VividRed else SurfaceGray
        val textColor = if (isAmplifierActive) White else Gray400
        val btnText = if (isAmplifierActive) "ON" else "OFF"
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(btnColor)
                .clickable { isAmplifierActive = !isAmplifierActive },
            contentAlignment = Alignment.Center
        ) {
            Text(btnText, color = textColor, fontSize = 48.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Master Volume Slider
        Text("Master Volume", color = Gray400, fontSize = 14.sp)
        Slider(
            value = masterVolume,
            onValueChange = { masterVolume = it },
            colors = SliderDefaults.colors(
                thumbColor = VividRed,
                activeTrackColor = VividRed,
                inactiveTrackColor = SurfaceGray
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Environment Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            presets.forEach { preset ->
                val isSelected = preset == selectedPreset
                Button(
                    onClick = { selectedPreset = preset },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) White else SurfaceGray,
                        contentColor = if (isSelected) PureBlack else Gray400
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(preset, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
