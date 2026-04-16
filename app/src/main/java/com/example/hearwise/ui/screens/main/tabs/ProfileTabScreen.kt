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
fun ProfileTabScreen(
    onRetakeTest: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
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

        // Audiogram canvas mapping stub
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .background(SurfaceGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "[Audiogram Graph Render Stub]\nLeft vs Right Ear Data",
                color = Gray400,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetakeTest,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White)
        ) {
            Text("Retake Test", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Manual Calibration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Manual EQ Stub
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth()
                .background(SurfaceGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text("Advanced Graphic Equalizer Stub", color = Gray400)
        }
    }
}
