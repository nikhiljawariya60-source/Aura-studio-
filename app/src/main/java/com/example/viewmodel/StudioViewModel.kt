package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiPhotoEditorRepository
import com.example.data.MediaExportManager
import com.example.model.AspectPreset
import com.example.model.BokehLevel
import com.example.model.CompareMode
import com.example.model.EditCategory
import com.example.model.EditStep
import com.example.model.OutputQuality
import com.example.model.SavedCreation
import com.example.model.StylePreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "StudioViewModel"

sealed interface GenerationStatus {
    data object Idle : GenerationStatus
    data object EnhancingPrompt : GenerationStatus
    data class Generating(val category: EditCategory) : GenerationStatus
    data class Success(val message: String) : GenerationStatus
    data class Error(val message: String) : GenerationStatus
}

sealed interface ExportStatus {
    data object Idle : ExportStatus
    data object Exporting : ExportStatus
    data class Exported(val message: String, val creation: SavedCreation) : ExportStatus
    data class NeedsSafDestination(val suggestedFilename: String) : ExportStatus
    data class Error(val message: String) : ExportStatus
}

data class StudioUiState(
    val history: List<EditStep> = emptyList(),
    val activeStepIndex: Int = -1,
    val selectedCategory: EditCategory = EditCategory.BACKGROUND,
    val promptsByCategory: Map<EditCategory, String> = mapOf(
        EditCategory.BACKGROUND to "",
        EditCategory.CLOTHES to "",
        EditCategory.HAIRSTYLE to ""
    ),
    val selectedPresetIdByCategory: Map<EditCategory, String?> = emptyMap(),
    val selectedQuality: OutputQuality = OutputQuality.HD_2K,
    val selectedAspect: AspectPreset = AspectPreset.AUTO,
    val matchLighting: Boolean = true,
    val bokehLevel: BokehLevel = BokehLevel.SHALLOW,
    val compareMode: CompareMode = CompareMode.EDITED,
    val generationStatus: GenerationStatus = GenerationStatus.Idle,
    val exportStatus: ExportStatus = ExportStatus.Idle,
    val savedCreations: List<SavedCreation> = emptyList(),
    val shareUri: Uri? = null
) {
    val originalBitmap: Bitmap?
        get() = history.firstOrNull()?.bitmap

    val activeStep: EditStep?
        get() = history.getOrNull(activeStepIndex)

    val activeBitmap: Bitmap?
        get() = activeStep?.bitmap

    val hasEdits: Boolean
        get() = activeStepIndex > 0

    val canUndo: Boolean
        get() = activeStepIndex > 0

    val canRedo: Boolean
        get() = activeStepIndex in 0 until (history.lastIndex)

    val currentPrompt: String
        get() = promptsByCategory[selectedCategory].orEmpty()

    val selectedPresetId: String?
        get() = selectedPresetIdByCategory[selectedCategory]
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = AiPhotoEditorRepository()

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        refreshSavedCreations()
    }

    fun refreshSavedCreations() {
        viewModelScope.launch {
            try {
                val items = MediaExportManager.loadSavedCreations(getApplication())
                _uiState.update { it.copy(savedCreations = items) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load saved creations", e)
            }
        }
    }

    fun loadImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(generationStatus = GenerationStatus.Idle) }
            try {
                val bitmap = MediaExportManager.decodeSampledBitmapFromUri(
                    context = getApplication(),
                    uri = uri
                )
                setBaseBitmap(bitmap, summary = "Original Photo")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image URI", e)
                _uiState.update {
                    it.copy(
                        generationStatus = GenerationStatus.Error(
                            e.localizedMessage ?: "Could not open the selected photo."
                        )
                    )
                }
            }
        }
    }

    fun loadSamplePortrait(@DrawableRes drawableResId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(generationStatus = GenerationStatus.Idle) }
            try {
                val bitmap = MediaExportManager.decodeDrawableBitmap(
                    context = getApplication(),
                    drawableResId = drawableResId
                )
                setBaseBitmap(bitmap, summary = "Sample Portrait")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load sample portrait", e)
                _uiState.update {
                    it.copy(
                        generationStatus = GenerationStatus.Error("Unable to load sample portrait.")
                    )
                }
            }
        }
    }

    fun loadSavedCreationIntoStudio(creation: SavedCreation) {
        viewModelScope.launch {
            try {
                val bitmap = MediaExportManager.decodeLocalFileBitmap(creation.filePath)
                if (bitmap != null) {
                    setBaseBitmap(bitmap, summary = creation.title)
                } else {
                    _uiState.update {
                        it.copy(generationStatus = GenerationStatus.Error("Saved image file is missing."))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load creation into studio", e)
                _uiState.update {
                    it.copy(generationStatus = GenerationStatus.Error("Could not load saved photo."))
                }
            }
        }
    }

    private fun setBaseBitmap(bitmap: Bitmap, summary: String) {
        val initialStep = EditStep(
            id = UUID.randomUUID().toString(),
            bitmap = bitmap,
            category = null,
            summary = summary,
            quality = OutputQuality.HD_2K
        )
        _uiState.update {
            it.copy(
                history = listOf(initialStep),
                activeStepIndex = 0,
                compareMode = CompareMode.EDITED,
                generationStatus = GenerationStatus.Idle,
                exportStatus = ExportStatus.Idle
            )
        }
    }

    fun selectCategory(category: EditCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updatePrompt(text: String) {
        _uiState.update { state ->
            val updatedPrompts = state.promptsByCategory.toMutableMap().apply {
                put(state.selectedCategory, text)
            }
            val updatedPresets = state.selectedPresetIdByCategory.toMutableMap().apply {
                put(state.selectedCategory, null)
            }
            state.copy(
                promptsByCategory = updatedPrompts,
                selectedPresetIdByCategory = updatedPresets
            )
        }
    }

    fun applyPreset(preset: StylePreset) {
        _uiState.update { state ->
            val updatedPrompts = state.promptsByCategory.toMutableMap().apply {
                put(preset.category, preset.prompt)
            }
            val updatedPresets = state.selectedPresetIdByCategory.toMutableMap().apply {
                put(preset.category, preset.id)
            }
            state.copy(
                selectedCategory = preset.category,
                promptsByCategory = updatedPrompts,
                selectedPresetIdByCategory = updatedPresets,
                generationStatus = GenerationStatus.Idle
            )
        }
    }

    fun setQuality(quality: OutputQuality) {
        _uiState.update { it.copy(selectedQuality = quality) }
    }

    fun setAspectPreset(aspectPreset: AspectPreset) {
        _uiState.update { it.copy(selectedAspect = aspectPreset) }
    }

    fun setMatchLighting(enabled: Boolean) {
        _uiState.update { it.copy(matchLighting = enabled) }
    }

    fun setBokehLevel(bokehLevel: BokehLevel) {
        _uiState.update { it.copy(bokehLevel = bokehLevel) }
    }

    fun setCompareMode(mode: CompareMode) {
        _uiState.update { it.copy(compareMode = mode) }
    }

    fun selectHistoryIndex(index: Int) {
        _uiState.update { state ->
            if (index in state.history.indices) {
                state.copy(
                    activeStepIndex = index,
                    compareMode = if (index == 0) CompareMode.ORIGINAL else CompareMode.EDITED
                )
            } else {
                state
            }
        }
    }

    fun undo() {
        _uiState.update { state ->
            if (state.canUndo) {
                val target = state.activeStepIndex - 1
                state.copy(
                    activeStepIndex = target,
                    compareMode = if (target == 0) CompareMode.ORIGINAL else CompareMode.EDITED
                )
            } else state
        }
    }

    fun redo() {
        _uiState.update { state ->
            if (state.canRedo) {
                state.copy(
                    activeStepIndex = state.activeStepIndex + 1,
                    compareMode = CompareMode.EDITED
                )
            } else state
        }
    }

    fun enhanceCurrentPrompt() {
        val state = _uiState.value
        if (state.generationStatus is GenerationStatus.EnhancingPrompt ||
            state.generationStatus is GenerationStatus.Generating
        ) return

        val category = state.selectedCategory
        val rawPrompt = state.currentPrompt

        viewModelScope.launch {
            _uiState.update { it.copy(generationStatus = GenerationStatus.EnhancingPrompt) }
            try {
                val enhanced = aiRepository.enhancePrompt(rawPrompt, category)
                _uiState.update { current ->
                    val updatedPrompts = current.promptsByCategory.toMutableMap().apply {
                        put(category, enhanced)
                    }
                    current.copy(
                        promptsByCategory = updatedPrompts,
                        generationStatus = GenerationStatus.Idle
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Prompt enhancement error", e)
                _uiState.update {
                    it.copy(
                        generationStatus = GenerationStatus.Error(
                            e.localizedMessage ?: "Could not enhance prompt."
                        )
                    )
                }
            }
        }
    }

    fun generateAiEdit() {
        val state = _uiState.value
        val source = state.activeBitmap ?: return
        val prompt = state.currentPrompt.trim()
        if (prompt.isBlank()) {
            _uiState.update {
                it.copy(
                    generationStatus = GenerationStatus.Error(
                        "Enter a prompt or tap a preset style first."
                    )
                )
            }
            return
        }
        if (state.generationStatus is GenerationStatus.Generating) return

        val category = state.selectedCategory
        val quality = state.selectedQuality
        val aspect = state.selectedAspect
        val matchLighting = state.matchLighting
        val bokeh = state.bokehLevel

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    generationStatus = GenerationStatus.Generating(category),
                    exportStatus = ExportStatus.Idle
                )
            }
            try {
                val editedBitmap = aiRepository.applyPhotoEdit(
                    sourceBitmap = source,
                    category = category,
                    prompt = prompt,
                    matchLighting = matchLighting,
                    bokehLevel = bokeh,
                    quality = quality,
                    aspectPreset = aspect
                )

                val summaryLabel = prompt.take(28).let {
                    if (prompt.length > 28) "$it…" else it
                }
                val newStep = EditStep(
                    id = UUID.randomUUID().toString(),
                    bitmap = editedBitmap,
                    category = category,
                    summary = summaryLabel,
                    quality = quality
                )

                _uiState.update { current ->
                    val truncatedHistory = if (current.activeStepIndex >= 0) {
                        current.history.take(current.activeStepIndex + 1)
                    } else {
                        emptyList()
                    }
                    val updatedHistory = truncatedHistory + newStep
                    current.copy(
                        history = updatedHistory,
                        activeStepIndex = updatedHistory.lastIndex,
                        compareMode = CompareMode.EDITED,
                        generationStatus = GenerationStatus.Success(
                            "${category.shortLabel} applied in ${editedBitmap.width}×${editedBitmap.height}px"
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                _uiState.update {
                    it.copy(
                        generationStatus = GenerationStatus.Error(
                            e.localizedMessage ?: "AI editing failed. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun downloadHdPhoto() {
        val state = _uiState.value
        val activeBitmap = state.activeBitmap ?: return
        val activeStep = state.activeStep ?: return
        if (state.exportStatus is ExportStatus.Exporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(exportStatus = ExportStatus.Exporting) }
            try {
                val categoryLabel = activeStep.category?.shortLabel ?: "Studio Edit"
                val title = activeStep.summary.ifBlank { "Aura Studio Creation" }
                val outcome = MediaExportManager.saveCreationHd(
                    context = getApplication(),
                    bitmap = activeBitmap,
                    title = title,
                    categoryLabel = categoryLabel
                )
                val updatedList = MediaExportManager.loadSavedCreations(getApplication())
                if (outcome.savedToDeviceGallery || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    _uiState.update {
                        it.copy(
                            savedCreations = updatedList,
                            exportStatus = ExportStatus.Exported(
                                message = "Saved HD (${activeBitmap.width}×${activeBitmap.height}) to Pictures/AuraStudio",
                                creation = outcome.creation
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            savedCreations = updatedList,
                            exportStatus = ExportStatus.NeedsSafDestination(
                                suggestedFilename = "AuraStudio_HD_${System.currentTimeMillis()}.png"
                            )
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "HD export failed", e)
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Error(
                            e.localizedMessage ?: "Could not save HD photo."
                        )
                    )
                }
            }
        }
    }

    fun exportToSafUri(uri: Uri) {
        val activeBitmap = _uiState.value.activeBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(exportStatus = ExportStatus.Exporting) }
            try {
                MediaExportManager.writeBitmapToSafUri(getApplication(), uri, activeBitmap)
                val latest = _uiState.value.savedCreations.firstOrNull()
                if (latest != null) {
                    _uiState.update {
                        it.copy(
                            exportStatus = ExportStatus.Exported(
                                message = "Saved HD (${activeBitmap.width}×${activeBitmap.height}) PNG",
                                creation = latest
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "SAF export failed", e)
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Error(
                            e.localizedMessage ?: "Failed to save image."
                        )
                    )
                }
            }
        }
    }

    fun prepareShareBitmap(bitmap: Bitmap? = _uiState.value.activeBitmap) {
        val target = bitmap ?: return
        viewModelScope.launch {
            try {
                val uri = MediaExportManager.createShareUri(getApplication(), target)
                _uiState.update { it.copy(shareUri = uri) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Share preparation failed", e)
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Error("Unable to prepare image for sharing.")
                    )
                }
            }
        }
    }

    fun consumeShareUri() {
        _uiState.update { it.copy(shareUri = null) }
    }

    fun deleteCreation(creation: SavedCreation) {
        viewModelScope.launch {
            try {
                val updated = MediaExportManager.deleteSavedCreation(getApplication(), creation)
                _uiState.update { it.copy(savedCreations = updated) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Delete creation failed", e)
            }
        }
    }

    fun dismissGenerationStatus() {
        _uiState.update {
            if (it.generationStatus is GenerationStatus.Error ||
                it.generationStatus is GenerationStatus.Success
            ) {
                it.copy(generationStatus = GenerationStatus.Idle)
            } else it
        }
    }

    fun dismissExportStatus() {
        _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
    }
}
