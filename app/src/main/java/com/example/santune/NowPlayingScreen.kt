package com.example.santune

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.santune.ui.theme.MusicGradientBottom
import com.example.santune.ui.theme.MusicGradientMidLower
import com.example.santune.ui.theme.MusicGradientMidUpper
import com.example.santune.ui.theme.MusicGradientTop
import kotlin.math.roundToLong

@Composable
fun NowPlayingScreen(
    song: Song,
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    isFavorite: Boolean,

    shuffleEnabled: Boolean,
    repeatMode: Int,

    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onFavoriteClick: () -> Unit,

    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onQueueClick: () -> Unit
) {

    val context = LocalContext.current

    val defaultArtwork =
        "android.resource://${context.packageName}/${R.drawable.default_album_art}"

    BackHandler {
        onBackClick()
    }

    // ==========================================
    // MAIN SANTUNE GRADIENT
    // ==========================================

    val musicGradient = Brush.verticalGradient(
        colors = listOf(
            MusicGradientTop,
            MusicGradientMidUpper,
            MusicGradientMidLower,
            MusicGradientBottom
        )
    )

    // ==========================================
    // SOFT UI-ONLY BEAT
    // ==========================================

    val beatTransition = rememberInfiniteTransition(
        label = "softBeat"
    )

    val beatScale by beatTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 850,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beatScale"
    )

    val glowScale by beatTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val glowAlpha by beatTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // ==========================================
    // SMOOTH ARTWORK ROTATION
    // ==========================================

    val rotationAngle = remember {
        Animatable(0f)
    }

    LaunchedEffect(isPlaying, song.id) {

        if (isPlaying) {

            while (true) {

                rotationAngle.animateTo(
                    targetValue =
                        rotationAngle.value + 360f,

                    animationSpec = tween(
                        durationMillis = 14000,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    // ==========================================
    // SCREEN
    // ==========================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .background(musicGradient)
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {

        // ======================================
        // TOP BAR
        // ======================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        onBackClick()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "‹",
                    fontSize = 34.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier.size(10.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        // ======================================
        // SMALL SONG INFORMATION
        // ======================================

        Text(
            text = song.title,

            fontSize = 18.sp,

            color = Color.White,

            fontWeight = FontWeight.Bold,

            textAlign = TextAlign.Center,

            maxLines = 1,

            overflow = TextOverflow.Ellipsis,

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(1.dp)
        )

        Text(
            text = song.artist,

            fontSize = 12.sp,

            color =
                Color.White.copy(
                    alpha = 0.72f
                ),

            fontWeight =
                FontWeight.Medium,

            textAlign =
                TextAlign.Center,

            maxLines = 1,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.fillMaxWidth()
        )

        if (song.album.isNotBlank()) {

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = song.album,

                fontSize = 10.sp,

                color =
                    Color.White.copy(
                        alpha = 0.50f
                    ),

                textAlign =
                    TextAlign.Center,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // ======================================
        // CIRCULAR ARTWORK + SOFT BEAT
        // ======================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),

            contentAlignment =
                Alignment.Center
        ) {

            // ----------------------------------
            // OUTER SOFT GLOW
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(350.dp)
                    .graphicsLayer {

                        scaleX =
                            if (isPlaying)
                                glowScale
                            else
                                1f

                        scaleY =
                            if (isPlaying)
                                glowScale
                            else
                                1f

                        alpha =
                            if (isPlaying)
                                glowAlpha
                            else
                                0.05f
                    }
                    .clip(CircleShape)
                    .background(
                        Color(0xFFB8D8FF)
                    )
            )

            // ----------------------------------
            // SECOND SOFT BEAT RING
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(342.dp)
                    .graphicsLayer {

                        scaleX =
                            if (isPlaying)
                                beatScale
                            else
                                1f

                        scaleY =
                            if (isPlaying)
                                beatScale
                            else
                                1f

                        alpha =
                            if (isPlaying)
                                0.10f
                            else
                                0.06f
                    }
                    .clip(CircleShape)
                    .background(
                        Color(0xFFE8F1FF)
                    )
            )

            // ----------------------------------
            // DARK INNER BACKGROUND
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF111417)
                    )
            )

            // ----------------------------------
            // ALBUM ART
            // ----------------------------------

            SubcomposeAsyncImage(
                model = song.albumArtUri,

                contentDescription =
                    "Album artwork",

                contentScale =
                    ContentScale.Crop,

                loading = {

                    SubcomposeAsyncImage(
                        model = defaultArtwork,

                        contentDescription =
                            "Default artwork",

                        contentScale =
                            ContentScale.Crop,

                        modifier = Modifier
                            .size(320.dp)
                            .clip(CircleShape)
                    )
                },

                error = {

                    SubcomposeAsyncImage(
                        model = defaultArtwork,

                        contentDescription =
                            "Default artwork",

                        contentScale =
                            ContentScale.Crop,

                        modifier = Modifier
                            .size(320.dp)
                            .clip(CircleShape)
                    )
                },

                modifier = Modifier
                    .size(320.dp)
                    .clip(CircleShape)
                    .graphicsLayer {

                        rotationZ =
                            rotationAngle.value

                        scaleX =
                            if (isPlaying)
                                1.015f
                            else
                                1f

                        scaleY =
                            if (isPlaying)
                                1.015f
                            else
                                1f
                    }
            )
        }

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        // ======================================
        // THIN STRAIGHT PROGRESS LINE
        // ======================================

        if (duration > 0) {

            val progress =
                (
                        currentPosition.toFloat() /
                                duration.toFloat()
                        )
                    .coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .pointerInput(duration) {

                        detectTapGestures { offset ->

                            val width =
                                size.width.toFloat()

                            if (width > 0f) {

                                val newProgress =
                                    (
                                            offset.x /
                                                    width
                                            )
                                        .coerceIn(
                                            0f,
                                            1f
                                        )

                                onSeek(
                                    (
                                            duration *
                                                    newProgress
                                            )
                                        .roundToLong()
                                )
                            }
                        }
                    },

                contentAlignment =
                    Alignment.CenterStart
            ) {

                // BACKGROUND LINE

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(
                                alpha = 0.20f
                            )
                        )
                )

                // PLAYED LINE

                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(
                                alpha = 0.90f
                            )
                        )
                )

                // SMALL PROGRESS DOT

                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(12.dp),

                    contentAlignment =
                        Alignment.CenterEnd
                ) {

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White
                            )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 2.dp
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        formatTime(
                            currentPosition
                        ),

                    fontSize = 10.sp,

                    color =
                        Color.White.copy(
                            alpha = 0.60f
                        )
                )

                Text(
                    text =
                        formatTime(duration),

                    fontSize = 10.sp,

                    color =
                        Color.White.copy(
                            alpha = 0.60f
                        )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // ======================================
        // MAIN PLAYBACK CONTROLS
        // ======================================

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // PREVIOUS

            IconButton(
                onClick =
                    onPreviousClick,

                modifier =
                    Modifier.size(64.dp)
            ) {

                Text(
                    text = "⏮",

                    fontSize = 29.sp,

                    color = Color.White
                )
            }

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            // PLAY / PAUSE

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White
                    )
                    .clickable {
                        onPlayPauseClick()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (isPlaying)
                            "Ⅱ"
                        else
                            "▶",

                    fontSize = 27.sp,

                    color =
                        Color(0xFF1E1F21),

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            // NEXT

            IconButton(
                onClick =
                    onNextClick,

                modifier =
                    Modifier.size(64.dp)
            ) {

                Text(
                    text = "⏭",

                    fontSize = 29.sp,

                    color = Color.White
                )
            }
        }

        Spacer(
            modifier = Modifier.height(60.dp)
        )

        // ======================================
        // FOUR BOTTOM BUTTONS
        // FAVORITE / SHUFFLE / REPEAT / QUEUE
        // ======================================

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceEvenly,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // ----------------------------------
            // FAVORITE
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha =
                                if (isFavorite)
                                    0.18f
                                else
                                    0.10f
                        )
                    )
                    .clickable {
                        onFavoriteClick()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (isFavorite)
                            "♥"
                        else
                            "♡",

                    fontSize = 24.sp,

                    color =
                        if (isFavorite)
                            Color(0xFFFF6680)
                        else
                            Color.White
                )
            }

            // ----------------------------------
            // SHUFFLE
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha =
                                if (shuffleEnabled)
                                    0.22f
                                else
                                    0.10f
                        )
                    )
                    .clickable {
                        onShuffle()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "⇄",

                    fontSize = 24.sp,

                    color = Color.White,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            // ----------------------------------
            // REPEAT
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha =
                                if (repeatMode != 0)
                                    0.22f
                                else
                                    0.10f
                        )
                    )
                    .clickable {
                        onRepeat()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (repeatMode == 2)
                            "↻1"
                        else
                            "↻",

                    fontSize = 23.sp,

                    color = Color.White,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            // ----------------------------------
            // QUEUE / PLAYLIST
            // ----------------------------------

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha = 0.10f
                        )
                    )
                    .clickable {
                        onQueueClick()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "☷",

                    fontSize = 24.sp,

                    color = Color.White,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}