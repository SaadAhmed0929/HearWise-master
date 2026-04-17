package com.example.hearwise.feature.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val VividRed = Color(0xFFFF0000)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HearingTestPromptScreen(
    onComplete: (micDenied: Boolean) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onComplete(false) // Granted, mic is not denied
        } else {
            onComplete(true)  // Denied
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "First-Time\nHearing Test",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = White,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "To ensure the best possible audio enhancement, let's take a quick 2-minute test to map your unique hearing profile.",
                fontSize = 16.sp,
                color = Gray400,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividRed,
                    contentColor = White
                )
            ) {
                Text(
                    text = "Take Test Now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { onComplete(true) } // Assume skip means denied/no-mic
            ) {
                Text(
                    text = "Skip for later",
                    color = Gray400,
                    fontSize = 14.sp
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceGray,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Gray400) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Microphone Access",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "To map your hearing, HearWise needs access to your microphone.",
                    fontSize = 15.sp,
                    color = Gray400,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        showBottomSheet = false
                        scope.launch { sheetState.hide() }
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VividRed,
                        contentColor = White
                    )
                ) {
                    Text("Allow Microphone", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
