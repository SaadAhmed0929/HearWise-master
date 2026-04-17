package com.example.hearwise.feature.dashboard

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioManager
import android.media.AudioDeviceInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close

private val PureBlack = Color(0xFF000000)
private val SurfaceGray = Color(0xFF141414)
private val VividRed = Color(0xFFFF0000)
private val White = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFF888888)

@Composable
fun HomeTabScreen(micDenied: Boolean = false) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    var isAmplifierActive by remember { mutableStateOf(false) }
    var masterVolume by remember { mutableFloatStateOf(0.5f) }
    var selectedPreset by remember { mutableStateOf("Normal") }
    val presets = listOf("Normal", "Restaurant", "TV")
    
    var deviceExpanded by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf("Default Output") }
    var showLatencyDialog by remember { mutableStateOf(false) }
    
    // Track output devices and their Bluetooth status
    var outputDevicesInfo by remember { mutableStateOf(emptyList<Pair<String, Boolean>>()) }

    fun updateDevices() {
        outputDevicesInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { device ->
            val type = device.type
            val isBt = type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    (android.os.Build.VERSION.SDK_INT >= 31 && (type == 26 || type == 27 || type == 30))
            device.productName.toString() to isBt
        }.distinctBy { it.first }
    }

    DisposableEffect(audioManager) {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
        }
        audioManager.registerAudioDeviceCallback(callback, null)
        updateDevices()
        onDispose { audioManager.unregisterAudioDeviceCallback(callback) }
    }

    val outputDevices = outputDevicesInfo.map { it.first }
    val isBluetoothSelected = outputDevicesInfo.find { it.first == selectedDevice }?.second ?: false

    LaunchedEffect(outputDevices) {
        if (outputDevices.isNotEmpty() && (selectedDevice == "Default Output" || selectedDevice !in outputDevices)) {
            selectedDevice = outputDevices.first()
        }
    }

    if (showLatencyDialog) {
        AlertDialog(
            onDismissRequest = { showLatencyDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Latency Info", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    IconButton(onClick = { showLatencyDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = White)
                    }
                }
            },
            text = {
                Text(
                    "Bluetooth devices introduce processing delay (latency) which can cause an echo effect during real-time amplification. For the best experience, we recommend using wired headphones.",
                    color = Gray400
                )
            },
            confirmButton = {
                TextButton(onClick = { showLatencyDialog = false }) {
                    Text("Got it", color = VividRed)
                }
            },
            containerColor = SurfaceGray,
            titleContentColor = White,
            textContentColor = Gray400,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasMicPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                    .clickable { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Mic permission required. Tap here to allow.", color = White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Device Selector
        Box {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(SurfaceGray, RoundedCornerShape(24.dp))
                    .clickable { deviceExpanded = true }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isBluetoothSelected) Color(0xFFFFA000) else Color.Green))
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDevice, color = White, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Device", tint = White)
            }
            DropdownMenu(
                expanded = deviceExpanded,
                onDismissRequest = { deviceExpanded = false },
                modifier = Modifier.background(SurfaceGray)
            ) {
                val devicesList = if (outputDevices.isEmpty()) listOf("Default Output") else outputDevices
                devicesList.forEach { devName ->
                    DropdownMenuItem(
                        text = { Text(devName, color = White) },
                        onClick = {
                            selectedDevice = devName
                            deviceExpanded = false
                        }
                    )
                }
            }
        }
        
        // Latency Warning (Only for BT)
        if (isBluetoothSelected) {
            Text(
                text = "High Latency Warning ⓘ", 
                color = Color(0xFFFFA000), 
                fontSize = 12.sp, 
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { showLatencyDialog = true }
            )
        } else {
            // Buffer space to keep layout consistent
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Power Toggle
        val btnColor = if (isAmplifierActive) VividRed else SurfaceGray
        val textColor = if (isAmplifierActive) White else Gray400
        val btnText = if (isAmplifierActive) "ON" else "OFF"
        
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(btnColor)
                .clickable { isAmplifierActive = !isAmplifierActive },
            contentAlignment = Alignment.Center
        ) {
            Text(btnText, color = textColor, fontSize = 48.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.weight(1f))

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
