package com.example.hearwise.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val VividRed = Color(0xFFFF0000)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@Composable
fun CaptionsTabScreen(
    viewModel: CaptionsViewModel = viewModel()
) {
    val transcribedText by viewModel.transcribedText.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.initializeWhisper(context)
    }
    
    var expanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    
    // Auto-scroll to bottom when new text is added
    LaunchedEffect(transcribedText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

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
            text = "Powered by Whisper C++ (Offline)",
            fontSize = 14.sp,
            color = VividRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Language Dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceGray, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(16.dp)
        ) {
            val selectedName = SupportedLanguages.find { it.second == selectedLanguage }?.first ?: "Auto-Detect"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Language: $selectedName",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = White)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceGray)
            ) {
                SupportedLanguages.forEach { (name, iso) ->
                    DropdownMenuItem(
                        text = { Text(name, color = White) },
                        onClick = {
                            viewModel.selectLanguage(iso)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large scrolling text view area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(SurfaceGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = if (transcribedText.isEmpty()) "Waiting for speech..." else transcribedText,
                color = if (transcribedText.isEmpty()) Gray400 else White,
                fontSize = 24.sp, // Large for accessibility
                lineHeight = 34.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.clearText() },
                modifier = Modifier
                    .weight(0.3f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGray, contentColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear", fontSize = 16.sp)
            }
            
            Button(
                onClick = { viewModel.toggleListening() },
                modifier = Modifier
                    .weight(0.7f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.DarkGray else VividRed, 
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isListening) "Stop Listening" else "Start Listening", 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
