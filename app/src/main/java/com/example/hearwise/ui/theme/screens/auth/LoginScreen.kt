package com.example.hearwise.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Color Tokens ────────────────────────────────────────────────────────────
private val PureBlack    = Color(0xFF000000)
private val DeepBlack    = Color(0xFF0A0A0A)
private val CardBlack    = Color(0xFF121212)
private val VividRed     = Color(0xFFFF0000)
private val RedGlow      = Color(0xFFFF3333)
private val PureWhite    = Color(0xFFFFFFFF)
private val MutedGray    = Color(0xFF999999)
private val SubtleLine   = Color(0xFF222222)

// ─── LoginScreen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onSignupClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Subtle animated glow
    val glowPulse = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowPulse.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // ── Decorative background glow ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.TopCenter)
                .drawBehind {
                    // Soft radial glow emanating from top-center
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividRed.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width * 0.72f
                        ),
                        radius = size.width * 0.72f,
                        center = Offset(size.width * 0.5f, 0f)
                    )
                }
        )

        // ── Decorative geometric dots ─────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { col ->
                        val isHighlighted = (row == 0 && col == 2) || (row == 1 && col == 1)
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isHighlighted) VividRed.copy(alpha = 0.7f)
                                    else SubtleLine
                                )
                        )
                    }
                }
            }
        }

        // ── Main content ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Brand mark ───────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Waveform icon — three bars of varying height
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(16.dp, 28.dp, 20.dp, 34.dp, 20.dp).forEachIndexed { i, h ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(h)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(listOf(RedGlow, VividRed))
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "HearWise",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Tagline ──────────────────────────────────────────────────
            Text(
                text = "Your intelligent hearing companion",
                fontSize = 13.sp,
                color = MutedGray,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // ── Section label ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(VividRed)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Email field ──────────────────────────────────────────────
            HearWiseTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email address",
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = VividRed,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Password field ───────────────────────────────────────────
            HearWiseTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = VividRed,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = MutedGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // ── Forgot password ──────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = { /* Handle Forgot Password */ },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Forgot password?",
                        fontSize = 12.sp,
                        color = VividRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Login button ─────────────────────────────────────────────
            Button(
                onClick = { onLoginClick(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividRed,
                    contentColor   = PureWhite
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation  = 0.dp
                )
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = SubtleLine
                )
                Text(
                    text = "  or  ",
                    fontSize = 11.sp,
                    color = MutedGray
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = SubtleLine
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sign up row ──────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New to HearWise?",
                    fontSize = 14.sp,
                    color = MutedGray
                )
                TextButton(
                    onClick = onSignupClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Create account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Bottom trust line ─────────────────────────────────────────
            Text(
                text = "Secured with end-to-end encryption",
                fontSize = 11.sp,
                color = MutedGray.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                letterSpacing = 0.2.sp
            )
        }
    }
}

// ─── Reusable styled text field ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HearWiseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MutedGray
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = VividRed,
            unfocusedBorderColor    = SubtleLine,
            focusedLabelColor       = VividRed,
            unfocusedLabelColor     = MutedGray,
            cursorColor             = VividRed,
            focusedTextColor        = PureWhite,
            unfocusedTextColor      = PureWhite,
            focusedContainerColor   = DeepBlack,
            unfocusedContainerColor = DeepBlack
        )
    )
}