package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import com.example.model.SavedCreation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

data class ExportOutcome(
    val creation: SavedCreation,
    val savedToDeviceGallery: Boolean
)

object MediaExportManager {

    private const val CREATIONS_DIR = "creations"
    private const val INDEX_FILE = "creations_index.json"
    private const val SHARE_DIR = "shared_images"

    suspend fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = 2048
    ): Bitmap = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
                val width = info.size.width
                val height = info.size.height
                val longest = max(width, height)
                if (longest > maxDimension) {
                    val scale = maxDimension.toFloat() / longest.toFloat()
                    decoder.setTargetSize(
                        (width * scale).roundToInt().coerceAtLeast(1),
                        (height * scale).roundToInt().coerceAtLeast(1)
                    )
                }
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            var inSampleSize = 1
            val longest = max(bounds.outWidth, bounds.outHeight)
            while (longest / inSampleSize > maxDimension) {
                inSampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalStateException("Could not decode the selected image.")

            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            applyExifOrientation(decoded, orientation)
        }
    }

    suspend fun decodeDrawableBitmap(
        context: Context,
        @DrawableRes drawableResId: Int
    ): Bitmap = withContext(Dispatchers.IO) {
        BitmapFactory.decodeResource(context.resources, drawableResId)
            ?: throw IllegalStateException("Unable to load sample studio portrait.")
    }

    suspend fun decodeLocalFileBitmap(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext null
        BitmapFactory.decodeFile(file.absolutePath)
    }

    suspend fun saveCreationHd(
        context: Context,
        bitmap: Bitmap,
        title: String,
        categoryLabel: String
    ): ExportOutcome = withContext(Dispatchers.IO) {
        val creationId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val fileName = "AuraStudio_${timestamp}.png"

        val creationsDir = File(context.filesDir, CREATIONS_DIR).apply {
            if (!exists()) mkdirs()
        }
        val localFile = File(creationsDir, fileName)
        FileOutputStream(localFile).use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IllegalStateException("Failed to encode HD PNG image.")
            }
        }

        var mediaStoreUriString: String? = null
        var savedToGallery = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.WIDTH, bitmap.width)
                put(MediaStore.Images.Media.HEIGHT, bitmap.height)
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/AuraStudio"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val insertedUri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (insertedUri != null) {
                try {
                    resolver.openOutputStream(insertedUri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    val publishValues = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    resolver.update(insertedUri, publishValues, null, null)
                    mediaStoreUriString = insertedUri.toString()
                    savedToGallery = true
                } catch (e: Exception) {
                    resolver.delete(insertedUri, null, null)
                    throw e
                }
            }
        }

        val creation = SavedCreation(
            id = creationId,
            filePath = localFile.absolutePath,
            mediaStoreUri = mediaStoreUriString,
            title = title,
            categoryLabel = categoryLabel,
            width = bitmap.width,
            height = bitmap.height,
            createdAtMillis = timestamp
        )
        appendCreationIndex(context, creation)

        ExportOutcome(
            creation = creation,
            savedToDeviceGallery = savedToGallery
        )
    }

    suspend fun writeBitmapToSafUri(
        context: Context,
        uri: Uri,
        bitmap: Bitmap
    ) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IllegalStateException("Could not write HD PNG to chosen destination.")
            }
        } ?: throw IllegalStateException("Unable to open destination file.")
    }

    suspend fun createShareUri(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val shareDir = File(context.cacheDir, SHARE_DIR).apply {
            if (!exists()) mkdirs()
        }
        val shareFile = File(shareDir, "AuraStudio_HD_${System.currentTimeMillis()}.png")
        FileOutputStream(shareFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile
        )
    }

    suspend fun loadSavedCreations(context: Context): List<SavedCreation> =
        withContext(Dispatchers.IO) {
            val indexFile = File(File(context.filesDir, CREATIONS_DIR), INDEX_FILE)
            if (!indexFile.exists()) return@withContext emptyList()
            val raw = indexFile.readText()
            if (raw.isBlank()) return@withContext emptyList()

            val array = JSONArray(raw)
            val list = mutableListOf<SavedCreation>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val filePath = obj.getString("filePath")
                if (File(filePath).exists()) {
                    list.add(
                        SavedCreation(
                            id = obj.getString("id"),
                            filePath = filePath,
                            mediaStoreUri = obj.optString("mediaStoreUri").takeIf { it.isNotBlank() },
                            title = obj.getString("title"),
                            categoryLabel = obj.getString("categoryLabel"),
                            width = obj.getInt("width"),
                            height = obj.getInt("height"),
                            createdAtMillis = obj.getLong("createdAtMillis")
                        )
                    )
                }
            }
            list.sortedByDescending { it.createdAtMillis }
        }

    suspend fun deleteSavedCreation(
        context: Context,
        creation: SavedCreation
    ): List<SavedCreation> = withContext(Dispatchers.IO) {
        File(creation.filePath).delete()
        val existing = loadSavedCreations(context).filterNot { it.id == creation.id }
        writeCreationIndex(context, existing)
        existing
    }

    private fun appendCreationIndex(context: Context, creation: SavedCreation) {
        val dir = File(context.filesDir, CREATIONS_DIR).apply {
            if (!exists()) mkdirs()
        }
        val indexFile = File(dir, INDEX_FILE)
        val existing = if (indexFile.exists() && indexFile.readText().isNotBlank()) {
            JSONArray(indexFile.readText())
        } else {
            JSONArray()
        }
        val entry = JSONObject().apply {
            put("id", creation.id)
            put("filePath", creation.filePath)
            put("mediaStoreUri", creation.mediaStoreUri ?: "")
            put("title", creation.title)
            put("categoryLabel", creation.categoryLabel)
            put("width", creation.width)
            put("height", creation.height)
            put("createdAtMillis", creation.createdAtMillis)
        }
        val updated = JSONArray().apply {
            put(entry)
            for (i in 0 until existing.length()) {
                put(existing.getJSONObject(i))
            }
        }
        indexFile.writeText(updated.toString())
    }

    private fun writeCreationIndex(context: Context, creations: List<SavedCreation>) {
        val dir = File(context.filesDir, CREATIONS_DIR).apply {
            if (!exists()) mkdirs()
        }
        val array = JSONArray()
        creations.forEach { creation ->
            array.put(
                JSONObject().apply {
                    put("id", creation.id)
                    put("filePath", creation.filePath)
                    put("mediaStoreUri", creation.mediaStoreUri ?: "")
                    put("title", creation.title)
                    put("categoryLabel", creation.categoryLabel)
                    put("width", creation.width)
                    put("height", creation.height)
                    put("createdAtMillis", creation.createdAtMillis)
                }
            )
        }
        File(dir, INDEX_FILE).writeText(array.toString())
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
