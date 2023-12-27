package ba.unsa.etf

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getLabels(imageUri: Uri, context: Context): List<String> = withContext(Dispatchers.IO) {
    try {
        val image = InputImage.fromFilePath(context, imageUri)
        val options = ImageLabelerOptions.Builder().build()
        val labeler = ImageLabeling.getClient(options)
        val task = labeler.process(image)
        val imageLabels = Tasks.await(task)

        var outputLabels: List<String> = listOf()
        for (label in imageLabels) {
            outputLabels = outputLabels + label.text
        }

        return@withContext outputLabels

    } catch (e: Exception) {
        Log.e("ImageClassification", "Error processing image", e)
        throw e
    }
}