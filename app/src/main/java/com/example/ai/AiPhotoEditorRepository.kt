package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.AspectPreset
import com.example.model.BokehLevel
import com.example.model.EditCategory
import com.example.model.OutputQuality
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "AiPhotoEditorRepo"
private const val IMAGE_MODEL_NAME = "gemini-nano-banana-2.1"
private const val PROMPT_MODEL_NAME = "gemini-3.8-flash"
private const val MAX_INPUT_DIMENSION = 1536
private const val GENERATION_TIMEOUT_MS = 60_000L
private const val ENHANCE_TIMEOUT_MS = 30_000L

class AiPhotoEditorRepository {

    private val promptEnhancerModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(modelName = PROMPT_MODEL_NAME)
    }

    private fun buildImageModel(
        quality: OutputQuality,
        aspectPreset: AspectPreset
    ): GenerativeModel {
        val imageSize = when (quality) {
            OutputQuality.STANDARD_1K -> ImageSize.SIZE_1K
            OutputQuality.HD_2K -> ImageSize.SIZE_2K
        }
        val aspectRatio = when (aspectPreset) {
            AspectPreset.AUTO -> null
            AspectPreset.PORTRAIT_3_4 -> AspectRatio.PORTRAIT_3x4
            AspectPreset.SQUARE_1_1 -> AspectRatio.SQUARE_1x1
            AspectPreset.LANDSCAPE_16_9 -> AspectRatio.LANDSCAPE_16x9
            AspectPreset.STORY_9_16 -> AspectRatio.PORTRAIT_9x16
        }

        val imageConfigBuilder = ImageConfig.builder()
            .setImageSize(imageSize)
        if (aspectRatio != null) {
            imageConfigBuilder.setAspectRatio(aspectRatio)
        }

        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
            imageConfig = imageConfigBuilder.build()
        }

        return Firebase.ai.generativeModel(
            modelName = IMAGE_MODEL_NAME,
            generationConfig = config
        )
    }

    suspend fun enhancePrompt(
        rawPrompt: String,
        category: EditCategory
    ): String {
        val trimmed = rawPrompt.trim()
        val systemInstruction = when (category) {
            EditCategory.BACKGROUND ->
                "Rewrite this user background idea into one vivid, concise, photorealistic photography backdrop prompt (20-38 words). Include lighting direction, atmosphere, depth, and realistic environment details. Output only the improved prompt text with no quotes or labels."
            EditCategory.CLOTHES ->
                "Rewrite this user outfit idea into one vivid, concise fashion photography wardrobe description (18-32 words). Specify garment cut, fabric texture, color palette, and layering. Output only the improved prompt text with no quotes or labels."
            EditCategory.HAIRSTYLE ->
                "Rewrite this user hairstyle idea into one vivid, concise editorial salon description (15-28 words). Specify cut, length, texture, parting, and color dimension. Output only the improved prompt text with no quotes or labels."
        }

        val userSeed = if (trimmed.isBlank()) {
            when (category) {
                EditCategory.BACKGROUND -> "cinematic sunset cityscape terrace with warm light"
                EditCategory.CLOTHES -> "modern tailored luxury casual outfit"
                EditCategory.HAIRSTYLE -> "effortless textured studio haircut"
            }
        } else {
            trimmed
        }

        return try {
            withTimeout(ENHANCE_TIMEOUT_MS) {
                val response = promptEnhancerModel.generateContent(
                    content {
                        text("$systemInstruction\n\nUser idea: $userSeed")
                    }
                )
                val enhanced = response.text?.trim()?.removeSurrounding("\"")
                if (!enhanced.isNullOrBlank()) enhanced else userSeed
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Prompt enhancement failed", e)
            throw IllegalStateException(
                e.localizedMessage ?: "Failed to enhance prompt.",
                e
            )
        }
    }

    suspend fun applyPhotoEdit(
        sourceBitmap: Bitmap,
        category: EditCategory,
        prompt: String,
        matchLighting: Boolean,
        bokehLevel: BokehLevel,
        quality: OutputQuality,
        aspectPreset: AspectPreset
    ): Bitmap {
        val preparedBitmap = withContext(Dispatchers.Default) {
            prepareBitmapForUpload(sourceBitmap)
        }
        val instruction = buildEditingInstruction(
            category = category,
            prompt = prompt.trim(),
            matchLighting = matchLighting,
            bokehLevel = bokehLevel
        )
        val model = buildImageModel(quality = quality, aspectPreset = aspectPreset)

        return try {
            withTimeout(GENERATION_TIMEOUT_MS) {
                val response = model.generateContent(
                    content {
                        image(preparedBitmap)
                        text(instruction)
                    }
                )
                val imagePart = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()

                imagePart?.image
                    ?: throw IllegalStateException("The AI studio model did not return an edited image. Try refining your prompt.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI photo editing timed out", e)
            throw IllegalStateException("Image generation timed out after 60 seconds. Please try again.", e)
        } catch (e: QuotaExceededException) {
            Log.e(TAG, "Firebase AI quota exceeded", e)
            throw IllegalStateException("AI studio rate limit reached. Please wait a moment and try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI photo edit failed", e)
            throw IllegalStateException(
                e.localizedMessage ?: "AI photo editing failed. Please try again.",
                e
            )
        }
    }

    private fun buildEditingInstruction(
        category: EditCategory,
        prompt: String,
        matchLighting: Boolean,
        bokehLevel: BokehLevel
    ): String = when (category) {
        EditCategory.BACKGROUND -> buildString {
            append("Edit the provided photograph by replacing ONLY the background environment with the following scene: ")
            append(prompt)
            append(". ")
            append("Preserve the foreground subject's exact face, identity, expression, skin tone, hair strands, body pose, and clothing completely unchanged. ")
            append("Maintain crisp, natural hair and shoulder edge separation with zero halo artifacts. ")
            if (matchLighting) {
                append("Subtly harmonize ambient rim light, color temperature, and ground/contact shadows on the subject so they belong naturally inside the new environment. ")
            }
            append("Render the background with ${bokehLevel.promptDirective}. Photorealistic, high-definition DSLR photography.")
        }

        EditCategory.CLOTHES -> buildString {
            append("Edit the provided photograph by changing ONLY the subject's clothing/outfit to: ")
            append(prompt)
            append(". ")
            append("Preserve the person's exact face, facial features, identity, hairstyle, skin tone, body posture, hands, and the entire background unchanged. ")
            append("Ensure realistic fabric weave, natural garment folds, tailored seams, and shadows matching the scene's original lighting. Photorealistic high-definition portrait.")
        }

        EditCategory.HAIRSTYLE -> buildString {
            append("Edit the provided photograph by changing ONLY the subject's hairstyle to: ")
            append(prompt)
            append(". ")
            append("Preserve the person's exact facial features, eye shape, expression, skin texture, clothing, pose, and background completely unchanged. ")
            append("Render realistic individual hair strands, natural hairline, volume, and light sheen matching the original photograph's lighting. Photorealistic high-definition portrait.")
        }
    }

    private fun prepareBitmapForUpload(bitmap: Bitmap): Bitmap {
        val longestSide = max(bitmap.width, bitmap.height)
        if (longestSide <= MAX_INPUT_DIMENSION) return bitmap
        val scale = MAX_INPUT_DIMENSION.toFloat() / longestSide.toFloat()
        val targetWidth = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
