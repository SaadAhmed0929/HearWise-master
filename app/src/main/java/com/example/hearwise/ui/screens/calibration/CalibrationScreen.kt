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
import com.example.hearwise.audio.SineWaveGenerator
import com.example.hearwise.data.HearingProfile
import com.example.hearwise.data.HearingThresholds
import com.example.hearwise.data.ProfileManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val PureBlack = Color(0xFF000000)
private val VividRed = Color(0xFFFF0000)
private val BlueLeft = Color(0xFF2196F3)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

enum class CaliPhase { PREP, LEFT_EAR, RIGHT_EAR, DONE }

@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var currentPhase by remember { mutableStateOf(CaliPhase.PREP) }
    
    // Testing Sequence
    val sequence = listOf(1000, 2000, 4000, 8000, 250, 500)
    var sequenceIndex by remember { mutableIntStateOf(0) }
    
    var currentVolume by remember { mutableFloatStateOf(0.0f) }
    val sineGen = remember { SineWaveGenerator() }

    // Data maps
    val leftMap = remember { mutableMapOf<Int, Int>() }
    val rightMap = remember { mutableMapOf<Int, Int>() }

    fun advanceSequence() {
        if (sequenceIndex < sequence.size - 1) {
            sequenceIndex++
        } else {
            // Reached end of sequence for an ear
            if (currentPhase == CaliPhase.LEFT_EAR) {
                currentPhase = CaliPhase.RIGHT_EAR
                sequenceIndex = 0
            } else {
                currentPhase = CaliPhase.DONE
                
                // Construct and save Profile
                val lt = HearingThresholds(
                    hz_250 = leftMap[250] ?: 0, hz_500 = leftMap[500] ?: 0,
                    hz_1000 = leftMap[1000] ?: 0, hz_2000 = leftMap[2000] ?: 0,
                    hz_4000 = leftMap[4000] ?: 0, hz_8000 = leftMap[8000] ?: 0
                )
                val rt = HearingThresholds(
                    hz_250 = rightMap[250] ?: 0, hz_500 = rightMap[500] ?: 0,
                    hz_1000 = rightMap[1000] ?: 0, hz_2000 = rightMap[2000] ?: 0,
                    hz_4000 = rightMap[4000] ?: 0, hz_8000 = rightMap[8000] ?: 0
                )
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val profile = HearingProfile(
                    timestamp = sdf.format(Date()),
                    leftEar = lt,
                    rightEar = rt
                )
                ProfileManager.saveProfile(context, profile)
            }
        }
    }

    // Audio Loop
    LaunchedEffect(currentPhase, sequenceIndex) {
        if (currentPhase == CaliPhase.LEFT_EAR || currentPhase == CaliPhase.RIGHT_EAR) {
            currentVolume = 0.0f
            val freq = sequence[sequenceIndex]
            val isLeft = currentPhase == CaliPhase.LEFT_EAR
            
            sineGen.startTone(freq.toDouble(), isLeft)
            
            while (currentVolume <= 1.0f) {
                sineGen.setVolume(currentVolume)
                delay(1500)
                currentVolume += 0.05f 
            }
            
            // Loop naturally finishes without them hearing it (deaf at freq)
            if (currentVolume >= 1.0f) {
                val dbInt = 100
                if (isLeft) leftMap[freq] = dbInt else rightMap[freq] = dbInt
                advanceSequence()
            }
        } else {
            sineGen.stopTone()
        }
    }

    // Cleanup cleanly
    DisposableEffect(Unit) {
        onDispose { sineGen.stopTone() }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(PureBlack).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = currentPhase, label = "cali_phase") { phase ->
            when (phase) {
                CaliPhase.PREP -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Calibration", fontSize = 32.sp, color = White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "• Find a quiet room.\n• Connect your earbuds.\n• Set phone volume to 50%.", 
                            color = Gray400, fontSize = 18.sp, lineHeight = 32.sp, textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = { currentPhase = CaliPhase.LEFT_EAR },
                            colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White),
                            modifier = Modifier.height(56.dp).fillMaxWidth()
                        ) {
                            Text("Start Calibration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                CaliPhase.LEFT_EAR, CaliPhase.RIGHT_EAR -> {
                    val isLeft = phase == CaliPhase.LEFT_EAR
                    val freq = sequence[sequenceIndex]
                    
                    // Simple Progress mapping
                    val totalSteps = sequence.size * 2
                    val currentStep = sequenceIndex + if (isLeft) 0 else sequence.size
                    val progress = currentStep.toFloat() / totalSteps.toFloat()

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = VividRed,
                            trackColor = Gray400
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = if (isLeft) "Testing Left Ear..." else "Testing Right Ear...",
                            fontSize = 24.sp, color = if(isLeft) BlueLeft else VividRed, fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${freq}Hz", fontSize = 48.sp, color = White, fontWeight = FontWeight.Black)
                        
                        Spacer(modifier = Modifier.height(64.dp))
                        
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Record volume (convert amplitude percentage to db int stub)
                                val dbInt = (currentVolume * 100).toInt()
                                if (isLeft) leftMap[freq] = dbInt else rightMap[freq] = dbInt
                                advanceSequence()
                            },
                            modifier = Modifier.size(260.dp).clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = VividRed, contentColor = White)
                        ) {
                            Text("I Hear It", fontSize = 32.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                CaliPhase.DONE -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Results", fontSize = 32.sp, color = White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Render Graph
                        AudiogramCanvas(leftMap, rightMap)

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = {
                                    sequenceIndex = 0
                                    leftMap.clear()
                                    rightMap.clear()
                                    currentPhase = CaliPhase.PREP
                                },
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Text("Retake Test", color = White)
                            }
                            Button(
                                onClick = onCalibrationComplete,
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
    
    Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color(0xFF141414))) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val width = size.width
            val height = size.height
            val stepX = width / (freqs.size - 1)
            
            // Draw axes
            drawLine(Color.DarkGray, Offset(0f, height), Offset(width, height), strokeWidth = 2f)
            drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, height), strokeWidth = 2f)
            
            fun mapPoint(freqIdx: Int, dbDelta: Int?): Offset {
                val db = dbDelta ?: 0
                val x = freqIdx * stepX
                val y = height - ((100 - db) / 100f * height) // Audiogram technically plots 0dB (normal) at top. Let's map small dB (good) to top.
                val yInvert = (db / 100f) * height // small volume = good = top
                return Offset(x, yInvert)
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
                drawCircle(color = BlueLeft, radius = 8f, center = pl)
                drawCircle(color = VividRed, radius = 8f, center = pr)
            }
            
            drawPath(path = leftPath, color = BlueLeft, style = Stroke(width = 4f))
            drawPath(path = rightPath, color = VividRed, style = Stroke(width = 4f))
        }
    }
}
