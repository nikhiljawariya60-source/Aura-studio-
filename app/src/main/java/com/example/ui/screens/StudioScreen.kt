package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FaceRetouchingNatural
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AspectPreset
import com.example.model.BokehLevel
import com.example.model.CompareMode
import com.example.model.EditCategory
import com.example.model.EditStep
import com.example.model.OutputQuality
import com.example.model.StudioPresets
import com.example.model.StylePreset
import com.example.ui.components.BeforeAfterCanvas
import com.example.viewmodel.ExportStatus
import com.example.viewmodel.GenerationStatus
import com.example.viewmodel.StudioUiState

@Composable
fun StudioScreen(
    uiState: StudioUiState,
    onPickPhoto: () -> Unit,
    onLoadSamplePortrait: () -> Unit,
    onSelectCategory: (EditCategory) -> Unit,
    onPromptChange: (String) -> Unit,
    onSelectPreset: (StylePreset) -> Unit,
    onEnhancePrompt: () -> Unit,
    onApplyEdit: () -> Unit,
    onSelectQuality: (OutputQuality) -> Unit,
    onSelectAspect: (AspectPreset) -> Unit,
    onToggleMatchLighting: (Boolean) -> Unit,
    onSelectBokeh: (BokehLevel) -> Unit,
    onCompareModeChange: (CompareMode) -> Unit,
    onSelectHistoryIndex: (Int) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDownloadHd: () -> Unit,
    onShare: () -> Unit,
    onDismissGenerationStatus: () -> Unit,
    onDismissExportStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBitmap = uiState.activeBitmap
    val originalBitmap = uiState.originalBitmap

    if (activeBitmap == null || originalBitmap == null) {
        StudioEmptyHero(
            onPickPhoto = onPickPhoto,
            onLoadSamplePortrait = onLoadSamplePortrait,
            modifier = modifier
        )
        return
    }

    val isGenerating = uiState.generationStatus is GenerationStatus.Generating
    val isEnhancing = uiState.generationStatus is GenerationStatus.EnhancingPrompt
    val isExporting = uiState.exportStatus is ExportStatus.Exporting

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val isWideLayout = maxWidth >= 760.dp

        if (isWideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.08f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioTopBar(
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        isExporting = isExporting,
                        onPickPhoto = onPickPhoto,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        onShare = onShare,
                        onDownloadHd = onDownloadHd
                    )
                    BeforeAfterCanvas(
                        originalBitmap = originalBitmap,
                        activeBitmap = activeBitmap,
                        compareMode = uiState.compareMode,
                        isGenerating = isGenerating,
                        generatingCategory = (uiState.generationStatus as? GenerationStatus.Generating)?.category,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    CanvasFooterControls(
                        uiState = uiState,
                        onCompareModeChange = onCompareModeChange,
                        onSelectHistoryIndex = onSelectHistoryIndex
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.92f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatusBanners(
                        generationStatus = uiState.generationStatus,
                        exportStatus = uiState.exportStatus,
                        onDismissGeneration = onDismissGenerationStatus,
                        onDismissExport = onDismissExportStatus,
                        onDownloadHd = onDownloadHd
                    )
                    EditorControlsDeck(
                        uiState = uiState,
                        isGenerating = isGenerating,
                        isEnhancing = isEnhancing,
                        onSelectCategory = onSelectCategory,
                        onPromptChange = onPromptChange,
                        onSelectPreset = onSelectPreset,
                        onEnhancePrompt = onEnhancePrompt,
                        onApplyEdit = onApplyEdit,
                        onSelectQuality = onSelectQuality,
                        onSelectAspect = onSelectAspect,
                        onToggleMatchLighting = onToggleMatchLighting,
                        onSelectBokeh = onSelectBokeh
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioTopBar(
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    isExporting = isExporting,
                    onPickPhoto = onPickPhoto,
                    onUndo = onUndo,
                    onRedo = onRedo,
                    onShare = onShare,
                    onDownloadHd = onDownloadHd
                )

                BeforeAfterCanvas(
                    originalBitmap = originalBitmap,
                    activeBitmap = activeBitmap,
                    compareMode = uiState.compareMode,
                    isGenerating = isGenerating,
                    generatingCategory = (uiState.generationStatus as? GenerationStatus.Generating)?.category,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                )

                CanvasFooterControls(
                    uiState = uiState,
                    onCompareModeChange = onCompareModeChange,
                    onSelectHistoryIndex = onSelectHistoryIndex
                )

                StatusBanners(
                    generationStatus = uiState.generationStatus,
                    exportStatus = uiState.exportStatus,
                    onDismissGeneration = onDismissGenerationStatus,
                    onDismissExport = onDismissExportStatus,
                    onDownloadHd = onDownloadHd
                )

                EditorControlsDeck(
                    uiState = uiState,
                    isGenerating = isGenerating,
                    isEnhancing = isEnhancing,
                    onSelectCategory = onSelectCategory,
                    onPromptChange = onPromptChange,
                    onSelectPreset = onSelectPreset,
                    onEnhancePrompt = onEnhancePrompt,
                    onApplyEdit = onApplyEdit,
                    onSelectQuality = onSelectQuality,
                    onSelectAspect = onSelectAspect,
                    onToggleMatchLighting = onToggleMatchLighting,
                    onSelectBokeh = onSelectBokeh
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StudioEmptyHero(
    onPickPhoto: () -> Unit,
    onLoadSamplePortrait: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aura Studio",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Hd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "FREE HD STUDIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable(onClick = onPickPhoto)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_sample),
                    contentDescription = "Sample studio portrait preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CapabilityTag("Background Prompts")
                        CapabilityTag("Clothes")
                        CapabilityTag("Hairstyles")
                    }
                    Text(
                        text = "Restyle Your Look & Generate Any Scene",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Upload a portrait to swap outfits, try new hair, or type a custom background prompt in 2K HD.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }

            Button(
                onClick = onPickPhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("upload_photo_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.upload_photo),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onLoadSamplePortrait,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("try_sample_button"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.try_sample),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CapabilityTag(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StudioTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    isExporting: Boolean,
    onPickPhoto: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onShare: () -> Unit,
    onDownloadHd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = onPickPhoto,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.testTag("replace_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.change_photo),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.testTag("undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = "Undo edit"
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.testTag("redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = "Redo edit"
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onShare,
                modifier = Modifier.testTag("share_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share edited photo"
                )
            }

            Button(
                onClick = onDownloadHd,
                enabled = !isExporting,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.testTag("download_hd_button")
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isExporting) "Saving…" else stringResource(R.string.download_hd),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CanvasFooterControls(
    uiState: StudioUiState,
    onCompareModeChange: (CompareMode) -> Unit,
    onSelectHistoryIndex: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.history.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompareChip(
                    label = stringResource(R.string.compare_edited),
                    selected = uiState.compareMode == CompareMode.EDITED,
                    onClick = { onCompareModeChange(CompareMode.EDITED) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("compare_edited_chip")
                )
                CompareChip(
                    label = stringResource(R.string.compare_split),
                    selected = uiState.compareMode == CompareMode.SPLIT,
                    onClick = { onCompareModeChange(CompareMode.SPLIT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("compare_split_chip")
                )
                CompareChip(
                    label = stringResource(R.string.compare_original),
                    selected = uiState.compareMode == CompareMode.ORIGINAL,
                    onClick = { onCompareModeChange(CompareMode.ORIGINAL) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("compare_original_chip")
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.history, key = { _, step -> step.id }) { index, step ->
                    HistoryStepPill(
                        step = step,
                        index = index,
                        isActive = index == uiState.activeStepIndex,
                        onClick = { onSelectHistoryIndex(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun HistoryStepPill(
    step: EditStep,
    index: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val thumb = remember(step.bitmap) { step.bitmap.asImageBitmap() }
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("history_step_$index"),
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                bitmap = thumb,
                contentDescription = step.summary,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = if (index == 0) "Original" else "Step $index",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = step.category?.shortLabel ?: "Source",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StatusBanners(
    generationStatus: GenerationStatus,
    exportStatus: ExportStatus,
    onDismissGeneration: () -> Unit,
    onDismissExport: () -> Unit,
    onDownloadHd: () -> Unit
) {
    AnimatedVisibility(
        visible = generationStatus is GenerationStatus.Error || generationStatus is GenerationStatus.Success,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (generationStatus) {
            is GenerationStatus.Error -> {
                FeedbackBanner(
                    isError = true,
                    message = generationStatus.message,
                    actionLabel = null,
                    onAction = {},
                    onDismiss = onDismissGeneration
                )
            }
            is GenerationStatus.Success -> {
                FeedbackBanner(
                    isError = false,
                    message = generationStatus.message,
                    actionLabel = "Save HD",
                    onAction = onDownloadHd,
                    onDismiss = onDismissGeneration
                )
            }
            else -> Unit
        }
    }

    AnimatedVisibility(
        visible = exportStatus is ExportStatus.Exported || exportStatus is ExportStatus.Error,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (exportStatus) {
            is ExportStatus.Exported -> {
                FeedbackBanner(
                    isError = false,
                    message = exportStatus.message,
                    actionLabel = null,
                    onAction = {},
                    onDismiss = onDismissExport
                )
            }
            is ExportStatus.Error -> {
                FeedbackBanner(
                    isError = true,
                    message = exportStatus.message,
                    actionLabel = null,
                    onAction = {},
                    onDismiss = onDismissExport
                )
            }
            else -> Unit
        }
    }
}

@Composable
private fun FeedbackBanner(
    isError: Boolean,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val container = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val content = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = content,
                modifier = Modifier.weight(1f)
            )
            if (actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = content, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss message",
                    tint = content,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorControlsDeck(
    uiState: StudioUiState,
    isGenerating: Boolean,
    isEnhancing: Boolean,
    onSelectCategory: (EditCategory) -> Unit,
    onPromptChange: (String) -> Unit,
    onSelectPreset: (StylePreset) -> Unit,
    onEnhancePrompt: () -> Unit,
    onApplyEdit: () -> Unit,
    onSelectQuality: (OutputQuality) -> Unit,
    onSelectAspect: (AspectPreset) -> Unit,
    onToggleMatchLighting: (Boolean) -> Unit,
    onSelectBokeh: (BokehLevel) -> Unit
) {
    val activeCategory = uiState.selectedCategory
    val presets = remember(activeCategory) { StudioPresets.presetsFor(activeCategory) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 3 AI Studio Category Tabs: Background, Clothes, Hairstyle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditCategory.entries.forEach { category ->
                val selected = category == activeCategory
                val icon = when (category) {
                    EditCategory.BACKGROUND -> Icons.Rounded.Landscape
                    EditCategory.CLOTHES -> Icons.Rounded.Checkroom
                    EditCategory.HAIRSTYLE -> Icons.Rounded.FaceRetouchingNatural
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isGenerating) { onSelectCategory(category) }
                        .testTag("tab_${category.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = category.shortLabel,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = category.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Prompt Input Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeCategory.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activeCategory.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !isEnhancing && !isGenerating, onClick = onEnhancePrompt)
                            .testTag("enhance_prompt_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isEnhancing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = "Enhance prompt with AI",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = if (isEnhancing) "Refining…" else stringResource(R.string.enhance_prompt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.currentPrompt,
                    onValueChange = onPromptChange,
                    enabled = !isGenerating,
                    placeholder = {
                        Text(
                            text = when (activeCategory) {
                                EditCategory.BACKGROUND -> stringResource(R.string.prompt_background_hint)
                                EditCategory.CLOTHES -> stringResource(R.string.prompt_clothes_hint)
                                EditCategory.HAIRSTYLE -> stringResource(R.string.prompt_hair_hint)
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    trailingIcon = {
                        if (uiState.currentPrompt.isNotEmpty() && !isGenerating) {
                            IconButton(onClick = { onPromptChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear prompt",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_input")
                )

                // Preset Inspiration Strip
                Text(
                    text = "Quick Styles",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        val isPicked = uiState.selectedPresetId == preset.id
                        Surface(
                            modifier = Modifier
                                .widthIn(min = 112.dp, max = 165.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isGenerating) { onSelectPreset(preset) }
                                .testTag("preset_${preset.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPicked) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isPicked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isPicked) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Background-specific Lighting & Depth Controls
        if (activeCategory == EditCategory.BACKGROUND) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Scene Realism",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.matchLighting,
                                onClick = { onToggleMatchLighting(!uiState.matchLighting) },
                                label = { Text("Match Lighting") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.WbSunny,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("toggle_match_lighting")
                            )
                        }
                        items(BokehLevel.entries) { bokeh ->
                            FilterChip(
                                selected = uiState.bokehLevel == bokeh,
                                onClick = { onSelectBokeh(bokeh) },
                                label = { Text(bokeh.label) },
                                modifier = Modifier.testTag("bokeh_${bokeh.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // HD Quality & Framing Row
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Export Resolution",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutputQuality.entries.forEach { quality ->
                            val selected = uiState.selectedQuality == quality
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isGenerating) { onSelectQuality(quality) }
                                    .testTag("quality_${quality.name.lowercase()}"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = quality.badge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AspectPreset.entries) { aspect ->
                        FilterChip(
                            selected = uiState.selectedAspect == aspect,
                            onClick = { onSelectAspect(aspect) },
                            label = { Text(aspect.label) },
                            modifier = Modifier.testTag("aspect_${aspect.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Primary AI Generation CTA
        Button(
            onClick = onApplyEdit,
            enabled = !isGenerating && uiState.currentPrompt.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("apply_edit_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.generating_status),
                    style = MaterialTheme.typography.labelLarge
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (activeCategory) {
                        EditCategory.BACKGROUND -> "Generate AI Background"
                        EditCategory.CLOTHES -> "Apply AI Outfit"
                        EditCategory.HAIRSTYLE -> "Apply AI Hairstyle"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
