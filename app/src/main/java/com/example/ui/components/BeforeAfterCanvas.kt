package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.CompareMode
import com.example.model.EditCategory
import kotlin.math.roundToInt

@Composable
fun BeforeAfterCanvas(
    originalBitmap: Bitmap,
    activeBitmap: Bitmap,
    compareMode: CompareMode,
    isGenerating: Boolean,
    generatingCategory: EditCategory?,
    modifier: Modifier = Modifier
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    val activeImageBitmap = remember(activeBitmap) { activeBitmap.asImageBitmap() }
    val originalImageBitmap = remember(originalBitmap) { originalBitmap.asImageBitmap() }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp)
            ),
        color = surfaceColor,
        tonalElevation = 2.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.14f),
                                surfaceColor
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.35f),
                            radius = size.maxDimension * 0.75f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

            when (compareMode) {
                CompareMode.ORIGINAL -> {
                    Image(
                        bitmap = originalImageBitmap,
                        contentDescription = "Original uploaded photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("canvas_original_image"),
                        contentScale = ContentScale.Fit
                    )
                }

                CompareMode.EDITED -> {
                    Image(
                        bitmap = activeImageBitmap,
                        contentDescription = "AI edited photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("canvas_edited_image"),
                        contentScale = ContentScale.Fit
                    )
                }

                CompareMode.SPLIT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(containerWidthPx) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (containerWidthPx > 0f) {
                                        splitFraction =
                                            (splitFraction + dragAmount / containerWidthPx)
                                                .coerceIn(0.08f, 0.92f)
                                    }
                                }
                            }
                    ) {
                        // Underlying edited layer (right side)
                        Image(
                            bitmap = activeImageBitmap,
                            contentDescription = "AI edited half",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Clipped original layer (left side)
                        Image(
                            bitmap = originalImageBitmap,
                            contentDescription = "Original half",
                            modifier = Modifier
                                .fillMaxSize()
                                .drawWithContent {
                                    clipRect(right = size.width * splitFraction) {
                                        this@drawWithContent.drawContent()
                                    }
                                },
                            contentScale = ContentScale.Fit
                        )

                        // Split divider line & draggable handle
                        val dividerOffsetPx = (containerWidthPx * splitFraction).roundToInt()
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(dividerOffsetPx - 1, 0) }
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.92f))
                        )

                        Surface(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset { IntOffset(dividerOffsetPx - 40, 0) }
                                .size(38.dp)
                                .testTag("split_compare_handle"),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.CompareArrows,
                                    contentDescription = "Drag to compare original and edited photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Corner compare labels
                        StudioCornerBadge(
                            text = "ORIGINAL",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        )
                        StudioCornerBadge(
                            text = "AI EDITED",
                            accentColor = secondaryColor,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        )
                    }
                }
            }

            // Bottom-left resolution badge
            if (!isGenerating) {
                val displayedBitmap =
                    if (compareMode == CompareMode.ORIGINAL) originalBitmap else activeBitmap
                StudioCornerBadge(
                    text = "${displayedBitmap.width}×${displayedBitmap.height}px",
                    accentColor = if (compareMode != CompareMode.ORIGINAL && activeBitmap != originalBitmap) {
                        secondaryColor
                    } else null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            // Active AI synthesis shimmer & progress overlay
            if (isGenerating) {
                GeneratingShimmerOverlay(
                    category = generatingCategory,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }
    }
}

@Composable
private fun StudioCornerBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.62f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (accentColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GeneratingShimmerOverlay(
    category: EditCategory?,
    primaryColor: Color,
    secondaryColor: Color
) {
    val transition = rememberInfiniteTransition(label = "ai_shimmer")
    val progress by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .drawBehind {
                val xStart = size.width * progress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.28f),
                            secondaryColor.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        start = Offset(xStart - 220f, 0f),
                        end = Offset(xStart + 220f, size.height)
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        color = secondaryColor,
                        strokeWidth = 3.dp
                    )
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = when (category) {
                        EditCategory.BACKGROUND -> "Generating AI Background…"
                        EditCategory.CLOTHES -> "Tailoring AI Outfit…"
                        EditCategory.HAIRSTYLE -> "Styling AI Hair…"
                        null -> "Applying AI Edit…"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rendering studio quality (30–60s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
