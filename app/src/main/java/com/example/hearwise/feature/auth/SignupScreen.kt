package com.example.hearwise.feature.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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

// ─── SignupScreen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name              by remember { mutableStateOf("") }
    var email             by remember { mutableStateOf("") }
    var password          by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Ambient glow pulse
    val glowPulse = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowPulse.animateFloat(
        initialValue  = 0.15f,
        targetValue   = 0.35f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {

        // ── Ambient glow — shifted to bottom-start for visual variety ──────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .align(Alignment.BottomCenter)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividRed.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.2f, size.height),
                            radius = size.width * 0.65f
                        ),
                        radius = size.width * 0.65f,
                        center = Offset(size.width * 0.2f, size.height)
                    )
                }
        )

        // ── Decorative dot grid — top-start mirror of LoginScreen ─────────
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 56.dp, start = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { col ->
                        val isHighlighted = (row == 2 && col == 0) || (row == 1 && col == 1)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(16.dp, 28.dp, 20.dp, 34.dp, 20.dp).forEach { h ->
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

            Text(
                text = "Create your account in seconds",
                fontSize = 13.sp,
                color = MutedGray,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                    text = "Get Started",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Full Name ────────────────────────────────────────────
            SignupTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full name",
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = VividRed,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Email ────────────────────────────────────────────
            SignupTextField(
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

            // ── Password ─────────────────────────────────────────
            SignupTextField(
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

            Spacer(modifier = Modifier.height(8.dp))

            // ── Password hint ────────────────────────────────────
            Text(
                text = "Must be at least 8 characters",
                fontSize = 11.sp,
                color = MutedGray.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Create account button ────────────────────────────
            Button(
                onClick = onSignupClick,
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
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = SubtleLine)
                Text(text = "  or  ", fontSize = 11.sp, color = MutedGray)
                Divider(modifier = Modifier.weight(1f), color = SubtleLine)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Already have account ─────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 14.sp,
                    color = MutedGray
                )
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Sign in",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Terms notice ──────────────────────────────────────────────
            Text(
                text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                fontSize = 11.sp,
                color = MutedGray.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                letterSpacing = 0.2.sp
            )
        }
    }
}

// ─── Reusable styled text field ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupTextField(
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
        label = { Text(text = label, fontSize = 13.sp, color = MutedGray) },
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