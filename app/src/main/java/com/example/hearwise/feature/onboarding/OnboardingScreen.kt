package com.example.hearwise.feature.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ─── Color Tokens ─────────────────────────────────────────────────────────────
private val Obsidian     = Color(0xFF000000)   // background matching PureBlack
private val Surface1     = Color(0xFF141414)   // cards / panels
private val Surface2     = Color(0xFF1E1E1E)   // slightly lifted
private val White        = Color(0xFFFFFFFF)   // primary text / key elements
private val OffWhite     = Color(0xFFF0F0F0)   // secondary white
private val Gray400      = Color(0xFF888888)   // muted labels
private val Gray200      = Color(0xFF333333)   // dividers / inactive
private val Gray100      = Color(0xFF222222)   // very subtle lines
private val VividRed     = Color(0xFFFF0000)   // Matches LoginScreen
private val Crimson      = VividRed            // Re-aliased to match brand

// ─── Onboarding pages ─────────────────────────────────────────────────────────
private data class OnboardingPage(
    val illustrationId: Int,
    val stepLabel: String,     // "01 / 03" style step
    val title: String,
    val accentWord: String,    // single word in Crimson
    val subtitle: String
)

private val pages = listOf(
    OnboardingPage(
        illustrationId = 0,
        stepLabel  = "01 / 04",
        title      = "Welcome",
        accentWord = "Welcome",
        subtitle   = "Turn your smartphone into a powerful hearing assistant."
    ),
    OnboardingPage(
        illustrationId = 1,
        stepLabel  = "02 / 04",
        title      = "Smart Processing",
        accentWord = "Smart",
        subtitle   = "Real-time audio enhancement tailored to your ears."
    ),
    OnboardingPage(
        illustrationId = 2,
        stepLabel  = "03 / 04",
        title      = "Live Transcription",
        accentWord = "Live",
        subtitle   = "Never miss a word with offline, real-time captions."
    ),
    OnboardingPage(
        illustrationId = 3,
        stepLabel  = "04 / 04",
        title      = "Custom Hearing\nProfile",
        accentWord = "Custom",
        subtitle   = "Your ears are unique. Take a quick 2-minute test to map your hearing and get a personalized audio experience."
    )
)

// ─── Root screen ──────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("HearWisePrefs", android.content.Context.MODE_PRIVATE)

    val finishWithPrefs = {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
        onFinish()
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    // Breathe / glow animation
    val pulse = rememberInfiniteTransition(label = "pulse")
    val breatheScale by pulse.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val glowAlpha by pulse.animateFloat(
        initialValue  = 0.15f,
        targetValue   = 0.35f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Slow orbital rotation
    val rotation = rememberInfiniteTransition(label = "rot")
    val angle by rotation.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {

        // ── Red breathing glow at top ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.TopCenter)
                .drawBehind {
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

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 58.dp, start = 28.dp, end = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Wordmark — white text, single red dot as brand mark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.hearwise.R.drawable.hearwise_logo),
                        contentDescription = "HearWise Logo",
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text          = "Hear",
                        fontWeight    = FontWeight.Black,
                        fontSize      = 20.sp,
                        letterSpacing = (-0.5).sp,
                        color         = White
                    )
                    Text(
                        text          = "Wise",
                        fontWeight    = FontWeight.Black,
                        fontSize      = 20.sp,
                        letterSpacing = (-0.5).sp,
                        color         = White
                    )
                }

                // Skip
                if (pagerState.currentPage < pages.size - 1) {
                    Text(
                        text       = "Skip",
                        fontSize   = 13.sp,
                        color      = Gray400,
                        fontWeight = FontWeight.Normal,
                        modifier   = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { finishWithPrefs() }
                    )
                }
            }

            // ── Pager ─────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                PageContent(
                    page         = pages[pageIndex],
                    angle        = angle,
                    breatheScale = breatheScale,
                    glowAlpha    = glowAlpha
                )
            }

            // ── Bottom controls ───────────────────────────────────────────
            BottomControls(
                currentPage = pagerState.currentPage,
                totalPages  = pages.size,
                onNext = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        finishWithPrefs()
                    }
                },
                onBack = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            )
        }
    }
}

// ─── Page content ─────────────────────────────────────────────────────────────
@Composable
private fun PageContent(
    page: OnboardingPage,
    angle: Float,
    breatheScale: Float,
    glowAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // ── Illustration ──────────────────────────────────────────────────
        IllustrationCanvas(
            id           = page.illustrationId,
            angle        = angle,
            breatheScale = breatheScale,
            glowAlpha    = glowAlpha,
            size         = 210.dp
        )

        Spacer(modifier = Modifier.height(44.dp))

        // ── Step counter — gray, understated ─────────────────────────────
        Text(
            text          = page.stepLabel,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Medium,
            color         = Gray400,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Title — white, with ONE word in red ───────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            page.title.split("\n").forEach { line ->
                Row {
                    line.split(" ").forEachIndexed { i, word ->
                        if (i > 0) Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text          = word,
                            fontSize      = 34.sp,
                            fontWeight    = FontWeight.Black,
                            letterSpacing = (-1.0).sp,
                            color         = if (word == page.accentWord) Crimson else White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Subtitle ──────────────────────────────────────────────────────
        Text(
            text          = page.subtitle,
            fontSize      = 15.sp,
            color         = Gray400,
            textAlign     = TextAlign.Center,
            lineHeight    = 25.sp,
            letterSpacing = 0.1.sp
        )
    }
}

// ─── Illustrations ────────────────────────────────────────────────────────────
@Composable
private fun IllustrationCanvas(
    id: Int,
    angle: Float,
    breatheScale: Float,
    glowAlpha: Float,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                when (id) {
                    0    -> drawSoundIllustration(angle, breatheScale, glowAlpha)
                    1    -> drawEqualizerIllustration(angle, breatheScale, glowAlpha)
                    2    -> drawHealthIllustration(angle, breatheScale, glowAlpha)
                    else -> drawProfileIllustration(angle, breatheScale, glowAlpha)
                }
            }
    )
}

// ── 0: Concentric rings — white/gray palette, one red orbiting dot ────────────
private fun DrawScope.drawSoundIllustration(
    angle: Float,
    breatheScale: Float,
    glowAlpha: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r  = size.width * 0.38f

    // Faint white background glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(White.copy(alpha = glowAlpha), Color.Transparent)
        ),
        radius = r * 1.5f, center = Offset(cx, cy)
    )

    // Concentric rings in shades of gray/white
    listOf(
        0.50f to 0.35f,
        0.68f to 0.18f,
        0.88f to 0.08f
    ).forEachIndexed { i, (frac, alpha) ->
        drawArc(
            color      = White.copy(alpha = alpha),
            startAngle = -105f + angle * 0.03f * (i + 1),
            sweepAngle = 210f,
            useCenter  = false,
            topLeft    = Offset(cx - r * frac, cy - r * frac),
            size       = Size(r * frac * 2, r * frac * 2),
            style      = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Thin full circle
    drawCircle(
        color  = White.copy(alpha = 0.06f),
        radius = r * 0.95f,
        center = Offset(cx, cy),
        style  = Stroke(width = 0.8.dp.toPx())
    )

    // Center disc — white gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(White, OffWhite.copy(alpha = 0.6f)),
            center = Offset(cx, cy)
        ),
        radius = r * 0.26f * breatheScale,
        center = Offset(cx, cy)
    )
    drawCircle(
        color  = Obsidian,
        radius = r * 0.09f,
        center = Offset(cx, cy)
    )

    // Orbiting dot — the ONLY red element in this illustration
    val ox = cx + r * 0.64f * cos(Math.toRadians(angle.toDouble())).toFloat()
    val oy = cy + r * 0.64f * sin(Math.toRadians(angle.toDouble())).toFloat()
    drawCircle(color = Crimson.copy(alpha = 0.20f), radius = 8.dp.toPx(),   center = Offset(ox, oy))
    drawCircle(color = Crimson,                     radius = 4.dp.toPx(),   center = Offset(ox, oy))
}

// ── 1: Segmented ring + EQ bars — white/gray, red tip on tallest bar ──────────
private fun DrawScope.drawEqualizerIllustration(
    angle: Float,
    breatheScale: Float,
    glowAlpha: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r  = size.width * 0.38f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(White.copy(alpha = glowAlpha), Color.Transparent)
        ),
        radius = r * 1.5f, center = Offset(cx, cy)
    )

    // 8-segment ring — alternating bright/dim white
    for (i in 0 until 8) {
        drawArc(
            color      = if (i % 2 == 0) White.copy(alpha = 0.45f)
            else White.copy(alpha = 0.08f),
            startAngle = i * 45f + angle * 0.20f,
            sweepAngle = 38f,
            useCenter  = false,
            topLeft    = Offset(cx - r * 0.92f, cy - r * 0.92f),
            size       = Size(r * 1.84f, r * 1.84f),
            style      = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // EQ bars — white gradient, tallest bar gets a red top cap
    val barW    = 7.dp.toPx()
    val barGap  = 5.dp.toPx()
    val heights = listOf(0.24f, 0.42f, 0.60f, 0.42f, 0.24f)
    val totalW  = heights.size * barW + (heights.size - 1) * barGap
    heights.forEachIndexed { i, h ->
        val isTallest = i == 2
        val bx  = cx - totalW / 2f + i * (barW + barGap)
        val bh  = r * h * breatheScale
        val by  = cy - bh / 2f
        // White bar body
        drawRoundRect(
            brush        = Brush.verticalGradient(
                colors = listOf(White.copy(alpha = 0.9f), White.copy(alpha = 0.3f)),
                startY = by, endY = by + bh
            ),
            topLeft      = Offset(bx, by),
            size         = Size(barW, bh),
            cornerRadius = CornerRadius(barW / 2f)
        )
        // Red cap on the tallest bar only
        if (isTallest) {
            drawRoundRect(
                color        = Crimson,
                topLeft      = Offset(bx, by),
                size         = Size(barW, barW),
                cornerRadius = CornerRadius(barW / 2f)
            )
        }
    }
}

// ── 2: ECG waveform — white line, single red pulse dot ───────────────────────
private fun DrawScope.drawHealthIllustration(
    angle: Float,
    breatheScale: Float,
    glowAlpha: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r  = size.width * 0.38f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(White.copy(alpha = glowAlpha), Color.Transparent)
        ),
        radius = r * 1.5f, center = Offset(cx, cy)
    )

    // Slow-rotating dot ring — all white/gray
    val dotCount = 40
    for (i in 0 until dotCount) {
        val a  = Math.toRadians((i * (360f / dotCount) + angle * 0.15f).toDouble())
        val dx = cx + r * 0.94f * cos(a).toFloat()
        val dy = cy + r * 0.94f * sin(a).toFloat()
        drawCircle(
            color  = White.copy(alpha = if (i % 5 == 0) 0.40f else 0.10f),
            radius = 1.6.dp.toPx(),
            center = Offset(dx, dy)
        )
    }

    // ECG waveform — white
    val waveW  = r * 1.55f
    val startX = cx - waveW / 2f
    val path   = Path()
    for (i in 0..80) {
        val t  = i / 80f
        val wx = startX + t * waveW
        val wy = when {
            t < 0.22f -> cy + sin(t * Math.PI.toFloat() * 1.5f) * r * 0.10f
            t < 0.32f -> cy - r * 0.40f * breatheScale
            t < 0.42f -> cy + r * 0.22f
            t < 0.50f -> cy
            else      -> cy + sin(t * Math.PI.toFloat() * 2.5f) * r * 0.07f
        }
        if (i == 0) path.moveTo(wx, wy) else path.lineTo(wx, wy)
    }
    drawPath(
        path  = path,
        brush = Brush.horizontalGradient(
            colors = listOf(
                White.copy(alpha = 0.05f),
                White.copy(alpha = 0.85f),
                White.copy(alpha = 0.05f)
            ),
            startX = startX, endX = startX + waveW
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Center pulse — white halo, red dot
    drawCircle(
        color  = White.copy(alpha = 0.08f),
        radius = 18.dp.toPx() * breatheScale,
        center = Offset(cx, cy)
    )
    drawCircle(
        color  = White.copy(alpha = 0.15f),
        radius = 11.dp.toPx() * breatheScale,
        center = Offset(cx, cy)
    )
    // The ONE red element — the pulse dot
    drawCircle(
        color  = Crimson,
        radius = 5.5.dp.toPx(),
        center = Offset(cx, cy)
    )
}

// ── 3: Audiogram Network map — white connecting lines with red data nodes ──────
private fun DrawScope.drawProfileIllustration(
    angle: Float,
    breatheScale: Float,
    glowAlpha: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r  = size.width * 0.38f

    // Soft glow base
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(White.copy(alpha = glowAlpha), Color.Transparent)
        ),
        radius = r * 1.5f, center = Offset(cx, cy)
    )

    // Outer framing circle
    drawCircle(
        color  = White.copy(alpha = 0.1f),
        radius = r * 0.95f,
        center = Offset(cx, cy),
        style  = Stroke(width = 1.dp.toPx())
    )
    
    // Rotating radar sweep
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(Color.Transparent, White.copy(alpha = 0.2f), Color.Transparent),
            center = Offset(cx, cy)
        ),
        startAngle = angle,
        sweepAngle = 90f,
        useCenter  = true,
        topLeft    = Offset(cx - r, cy - r),
        size       = Size(r * 2, r * 2)
    )

    // Data nodes forming an audiogram-like zig-zag
    val points = listOf(
        Offset(cx - r * 0.6f, cy - r * 0.3f * breatheScale),
        Offset(cx - r * 0.2f, cy + r * 0.1f * breatheScale),
        Offset(cx + r * 0.2f, cy - r * 0.5f * breatheScale),
        Offset(cx + r * 0.6f, cy + r * 0.4f * breatheScale)
    )

    // Connecting lines
    val path = Path()
    points.forEachIndexed { index, p ->
        if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    
    drawPath(
        path = path,
        color = White.copy(alpha = 0.5f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Draw the nodes
    points.forEachIndexed { index, p ->
        // the 3rd node is the "red" active one
        val isImportant = index == 2
        
        drawCircle(
            color = if (isImportant) Crimson else White.copy(alpha = 0.8f),
            radius = (if (isImportant) 6.dp.toPx() else 4.dp.toPx()) * (if (isImportant) breatheScale else 1f),
            center = p
        )
        
        // Halo around the red node
        if (isImportant) {
            drawCircle(
                color = Crimson.copy(alpha = 0.3f),
                radius = 14.dp.toPx() * breatheScale,
                center = p
            )
        }
    }
}

// ─── Bottom controls ──────────────────────────────────────────────────────────
@Composable
private fun BottomControls(
    currentPage: Int,
    totalPages: Int,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val isLast = currentPage == totalPages - 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(bottom = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Progress dots — gray inactive, white active ───────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(totalPages) { i ->
                val isActive = i == currentPage
                val dotWidth by animateDpAsState(
                    targetValue   = if (isActive) 24.dp else 6.dp,
                    animationSpec = tween(260),
                    label         = "dot$i"
                )
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(
                            if (isActive) White else Gray200
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── CTA button — VividRed fill to match Login. ─────────────
        Button(
            onClick   = onNext,
            modifier  = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape     = RoundedCornerShape(28.dp),
            colors    = ButtonDefaults.buttonColors(
                containerColor = VividRed,
                contentColor   = White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text          = if (isLast) "Get Started" else "Continue",
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color         = White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Back ghost ────────────────────────────────────────────────────
        if (currentPage > 0) {
            TextButton(
                onClick  = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "Back",
                    fontSize   = 14.sp,
                    color      = Gray400,
                    fontWeight = FontWeight.Normal
                )
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}