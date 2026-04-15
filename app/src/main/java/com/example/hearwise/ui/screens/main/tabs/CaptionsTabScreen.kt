package com.example.hearwise.ui.screens.main.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun CaptionsTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "Live Captions",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = White
        )
        Text(
            text = "Powered by Google ML Kit (Offline)",
            fontSize = 14.sp,
            color = VividRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large scrolling text view area stub
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(SurfaceGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Waiting for speech...",
                color = Gray400,
                fontSize = 18.sp,
                lineHeight = 26.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* Clear logic */ },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGray, contentColor = White)
            ) {
                Text("Clear")
            }
            Button(
                onClick = { /* Save logic */ },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White)
            ) {
                Text("Save Session")
            }
        }
    }
}
