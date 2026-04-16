package com.example.hearwise.ui.screens.calibration

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hearwise.data.ProfileManager
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

private val PureBlack = Color(0xFF000000)
private val VividRed = Color(0xFFFF0000)
private val BlueLeft = Color(0xFF2196F3)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit,
    viewModel: CalibrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize().background(PureBlack).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = uiState.phase, label = "cali_phase") { phase ->
            when (phase) {
                TestPhase.PREP -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Calibration", fontSize = 32.sp, color = White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "• Find a quiet room.\n• Connect your earbuds.\n• Ready when you are.", 
                            color = Gray400, fontSize = 18.sp, lineHeight = 32.sp, textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = { viewModel.startCalibration() },
                            colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White),
                            modifier = Modifier.height(56.dp).fillMaxWidth()
                        ) {
                            Text("Start Calibration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                TestPhase.TESTING -> {
                    val isLeft = uiState.activeEar == Ear.LEFT
                    val freq = uiState.currentFreq
                    val currentDb = uiState.currentDb

                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Text(
                            text = if (isLeft) "Testing Left Ear... $freq Hz" else "Testing Right Ear... $freq Hz",
                            fontSize = 20.sp, color = if(isLeft) BlueLeft else VividRed, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Current Volume: $currentDb dB", 
                            fontSize = 16.sp, color = Gray400, fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onIHeardIt()
                            },
                            modifier = Modifier.size(280.dp).clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("I Hear It", fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.0).sp)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onCannotHearIt()
                            },
                            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
                        ) {
                            Text("I Can't Hear This Tone", color = Gray400, fontSize = 15.sp, fontWeight = FontWeight.Normal)
                        }
                    }
                }
                TestPhase.DONE -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Results", fontSize = 32.sp, color = White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Render Graph
                        AudiogramCanvas(uiState.leftMap, uiState.rightMap)

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.resetTest() },
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Text("Retake Test", color = White)
                            }
                            Button(
                                onClick = {
                                    val profile = uiState.finalProfile
                                    if (profile != null) {
                                        ProfileManager.saveProfile(context, profile)
                                    }
                                    onCalibrationComplete()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Text("Apply Profile")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudiogramCanvas(leftMap: Map<Int, Int>, rightMap: Map<Int, Int>) {
    // Basic Canvas rendering an audiogram
    val freqs = listOf(250, 500, 1000, 2000, 4000, 8000)
    
    Box(modifier = Modifier.fillMaxWidth().height(320.dp).background(Color(0xFF141414))) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(start = 48.dp, end = 24.dp, top = 48.dp, bottom = 48.dp)) {
            val width = size.width
            val height = size.height
            val stepX = width / (freqs.size - 1)
            
            // Draw axes
            drawLine(Color.DarkGray, Offset(0f, height), Offset(width, height), strokeWidth = 2f)
            drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, height), strokeWidth = 2f)

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                // Draw Legend at Top
                val legendPaint = android.graphics.Paint().apply {
                    textSize = 36f
                    textAlign = android.graphics.Paint.Align.LEFT
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                
                legendPaint.color = android.graphics.Color.parseColor("#2196F3")
                canvas.nativeCanvas.drawText("Left Ear", 0f, -60f, legendPaint)
                
                legendPaint.color = android.graphics.Color.parseColor("#FF0000")
                canvas.nativeCanvas.drawText("Right Ear", 250f, -60f, legendPaint)

                // Draw Y-Axis labels (dB) & Horizontal Grid Lines
                paint.textAlign = android.graphics.Paint.Align.RIGHT
                listOf(0, 20, 40, 60, 80, 100).forEach { db ->
                    val y = (db / 100f) * height
                    // Faint grid
                    drawLine(Color.DarkGray.copy(alpha = 0.3f), Offset(0f, y), Offset(width, y), strokeWidth = 2f)
                    canvas.nativeCanvas.drawText("${db}dB", -16f, y + 10f, paint)
                }

                // Draw X-Axis labels (frequencies)
                paint.textAlign = android.graphics.Paint.Align.CENTER
                freqs.forEachIndexed { i, f ->
                    val x = i * stepX
                    // Faint vertical grid
                    drawLine(Color.DarkGray.copy(alpha = 0.3f), Offset(x, 0f), Offset(x, height), strokeWidth = 2f)
                    canvas.nativeCanvas.drawText("${f}Hz", x, height + 48f, paint)
                }
            }
            
            fun mapPoint(freqIdx: Int, dbDelta: Int?): Offset {
                // dB goes 0 to 85. 0 is Top, 85 is Bottom.
                val db = (dbDelta ?: 0).coerceIn(0, 100)
                val x = freqIdx * stepX
                val y = (db / 100f) * height
                return Offset(x, y)
            }

            // Draw lines 
            val leftPath = Path()
            val rightPath = Path()
            
            freqs.forEachIndexed { i, f ->
                val pl = mapPoint(i, leftMap[f])
                val pr = mapPoint(i, rightMap[f])
                if (i == 0) { leftPath.moveTo(pl.x, pl.y); rightPath.moveTo(pr.x, pr.y) } 
                else { leftPath.lineTo(pl.x, pl.y); rightPath.lineTo(pr.x, pr.y) }
                
                // Draw nodes
                drawCircle(color = BlueLeft, radius = 10f, center = pl)
                drawCircle(color = VividRed, radius = 10f, center = pr)
            }
            
            drawPath(path = leftPath, color = BlueLeft, style = Stroke(width = 6f))
            drawPath(path = rightPath, color = VividRed, style = Stroke(width = 6f))
        }
    }
}
