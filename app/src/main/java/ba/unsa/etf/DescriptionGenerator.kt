package ba.unsa.etf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun buildPrompt(labels: List<String>): String {
    return "Generate a description based on the following labels ${labels.joinToString(", ")}"
}

suspend fun sendApiRequest(apiKey: String, model: String, prompt: String): String? {
    return withContext(Dispatchers.IO) {
        val apiUrl = "https://api.openai.com/v1/engines/$model/completions"
        val mediaType = "application/json".toMediaType()
        val requestBody = buildJsonObject {
            put("prompt", prompt)
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            response.body?.string()
        } else {
            println("Error: ${response.code}")
            println("Error Body: ${response.body?.string()}")
            null
        }
    }
}

suspend fun generateDescription(labels: List<String>): String {
    val apiKey = "" //ADD OpenAI API key
    val model = "text-davinci-003"

    val prompt = buildPrompt(labels)
    val response = sendApiRequest(apiKey, model, prompt)

   return response ?: "No response available"
}

