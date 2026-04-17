package com.example.hearwise.ui.screens.main.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwise.data.HearingProfile
import com.example.hearwise.data.HearingThresholds
import com.example.hearwise.data.ProfileManager
import com.example.hearwise.ui.screens.calibration.AudiogramCanvas

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val VividRed = Color(0xFFFF0000)
private val BlueLeft = Color(0xFF2196F3)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@Composable
fun ProfileTabScreen(
    onRetakeTest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Parse Profile Data Natively
    var profile by remember { mutableStateOf(ProfileManager.loadProfile(context)) }
    
    val freqs = listOf(250, 500, 1000, 2000, 4000, 8000)
    
    val leftMap = remember { 
        mutableStateMapOf<Int, Int>().apply {
            val p = profile?.leftEar
            put(250, p?.hz_250 ?: 0)
            put(500, p?.hz_500 ?: 0)
            put(1000, p?.hz_1000 ?: 0)
            put(2000, p?.hz_2000 ?: 0)
            put(4000, p?.hz_4000 ?: 0)
            put(8000, p?.hz_8000 ?: 0)
        }
    }
    
    val rightMap = remember { 
        mutableStateMapOf<Int, Int>().apply {
            val p = profile?.rightEar
            put(250, p?.hz_250 ?: 0)
            put(500, p?.hz_500 ?: 0)
            put(1000, p?.hz_1000 ?: 0)
            put(2000, p?.hz_2000 ?: 0)
            put(4000, p?.hz_4000 ?: 0)
            put(8000, p?.hz_8000 ?: 0)
        }
    }

    // Natively reserializes when the tweaker overrides graphical DB points
    fun syncProfile() {
        val newProfile = HearingProfile(
            timestamp = profile?.timestamp ?: "",
            leftEar = HearingThresholds(
                hz_250 = leftMap[250] ?: 0, hz_500 = leftMap[500] ?: 0,
                hz_1000 = leftMap[1000] ?: 0, hz_2000 = leftMap[2000] ?: 0,
                hz_4000 = leftMap[4000] ?: 0, hz_8000 = leftMap[8000] ?: 0
            ),
            rightEar = HearingThresholds(
                hz_250 = rightMap[250] ?: 0, hz_500 = rightMap[500] ?: 0,
                hz_1000 = rightMap[1000] ?: 0, hz_2000 = rightMap[2000] ?: 0,
                hz_4000 = rightMap[4000] ?: 0, hz_8000 = rightMap[8000] ?: 0
            )
        )
        ProfileManager.saveProfile(context, newProfile)
        profile = newProfile
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Hearing Profile",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = White
        )
        Text(
            text = "Custom Audiogram Mapping",
            fontSize = 14.sp,
            color = VividRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reuse the dynamically drawn Canvas component
        AudiogramCanvas(leftMap = leftMap, rightMap = rightMap)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetakeTest,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White)
        ) {
            Text("Retake Threshold Sequence", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Manual Calibration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tweak frequency sound lines locally.",
            fontSize = 14.sp,
            color = Gray400
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Sliders block tracking the Left Map and Right Map manually 
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            freqs.forEach { freq ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${freq}Hz", color = White, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                    
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("L", color = BlueLeft, fontSize = 12.sp, modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold)
                            Slider(
                                value = (leftMap[freq] ?: 0).toFloat(),
                                onValueChange = { 
                                    leftMap[freq] = it.toInt()
                                    syncProfile()
                                },
                                valueRange = 0f..85f,
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                colors = SliderDefaults.colors(thumbColor = BlueLeft, activeTrackColor = BlueLeft, inactiveTrackColor = PureBlack)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R", color = VividRed, fontSize = 12.sp, modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold)
                            Slider(
                                value = (rightMap[freq] ?: 0).toFloat(),
                                onValueChange = { 
                                    rightMap[freq] = it.toInt()
                                    syncProfile()
                                },
                                valueRange = 0f..85f,
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                colors = SliderDefaults.colors(thumbColor = VividRed, activeTrackColor = VividRed, inactiveTrackColor = PureBlack)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}
