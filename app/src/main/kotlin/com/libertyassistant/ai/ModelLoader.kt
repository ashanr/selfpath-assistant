package com.libertyassistant.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ModelLoader {

    private const val MODELS_ASSET_DIR = "models"

    /**
     * Locates the first compatible model in assets and extracts it to the app's cache directory.
     * Returns null if no compatible model is found.
     */
    suspend fun extractModelToCache(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val availableModels = context.assets.list(MODELS_ASSET_DIR) ?: return@withContext null
            val modelFileName = availableModels.firstOrNull { name ->
                name.endsWith(".bin") || name.endsWith(".tflite") || name.endsWith(".task")
            } ?: return@withContext null

            val cacheDir = File(context.cacheDir, MODELS_ASSET_DIR).also { it.mkdirs() }
            val modelFile = File(cacheDir, modelFileName)

            if (!modelFile.exists()) {
                context.assets.open("$MODELS_ASSET_DIR/$modelFileName").use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
            }
            modelFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns all model files currently present in the assets/models directory.
     */
    fun listAvailableModels(context: Context): List<String> = try {
        context.assets.list(MODELS_ASSET_DIR)?.toList() ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    /**
     * Returns true if at least one compatible model is present in assets/models.
     */
    fun hasModel(context: Context): Boolean =
        listAvailableModels(context).any { it.endsWith(".bin") || it.endsWith(".tflite") }
}
