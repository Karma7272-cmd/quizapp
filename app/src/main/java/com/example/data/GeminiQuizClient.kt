package com.example.data

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiQuizClient {
    private const val TAG = "GeminiQuizClient"
    
    // OkHttp client with extended 60-second timeouts as per skill instructions
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateCustomQuiz(category: String, difficulty: String): List<Question>? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.e(TAG, "API Key is empty or placeholder!")
            return null
        }

        val prompt = """
            You are an advanced digital trivia master in a high-tech synthesizer arena.
            Generate a JSON list containing exactly 5 challenging and engaging trivia questions about '$category' with '$difficulty' difficulty.
            Respond with ONLY a raw, unencoded JSON array of objects. Do NOT wrap the JSON in markdown code blocks (such as ```json or ```). It must start with '[' and end with ']'.
            
            Each object in the array must strictly contain these exact keys:
            - "question": a string containing the question
            - "options": an array of exactly 4 strings representing multiple choice answers
            - "correctAnswer": a string matching exactly one of the four options
            - "explanation": a string with 1-2 sentences explaining why the answer is correct
            
            Strict Example Output format:
            [
              {
                "question": "What is the primary memory management system in Swift?",
                "options": ["Garbage Collection", "Automatic Reference Counting", "Manual Freeing", "Arena Allocation"],
                "correctAnswer": "Automatic Reference Counting",
                "explanation": "Swift uses ARC to track and manage your app's memory usage automatically by counting reference instances."
              }
            ]
        """.trimIndent()

        // Build Gemini Direct REST request payload using JSONObject
        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObject = JSONObject()
        val partsArray = JSONArray()
        val partObject = JSONObject()
        
        partObject.put("text", prompt)
        partsArray.put(partObject)
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        requestJson.put("contents", contentsArray)

        // Request structured JSON format
        val generationConfig = JSONObject()
        val responseFormat = JSONObject()
        val responseFormatText = JSONObject()
        responseFormatText.put("mimeType", "application/json")
        responseFormat.put("text", responseFormatText)
        generationConfig.put("responseFormat", responseFormat)
        requestJson.put("generationConfig", generationConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful response from Gemini: Code ${response.code}, Body: $errBody")
                    return null
                }
                
                val bodyString = response.body?.string() ?: return null
                Log.d(TAG, "Gemini Raw Response: $bodyString")
                
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates") ?: return null
                val firstCandidate = candidates.optJSONObject(0) ?: return null
                val content = firstCandidate.optJSONObject("content") ?: return null
                val parts = content.optJSONArray("parts") ?: return null
                val firstPart = parts.optJSONObject(0) ?: return null
                var text = firstPart.optString("text")?.trim() ?: return null
                
                // Defensive clean-up in case Gemini returns markdown tags despite system instruction
                if (text.startsWith("```")) {
                    text = text.removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()
                }
                
                Log.d(TAG, "Cleaned JSON Text: $text")
                
                val questionsArray = JSONArray(text)
                val list = mutableListOf<Question>()
                for (i in 0 until questionsArray.length()) {
                    val obj = questionsArray.getJSONObject(i)
                    val qText = obj.getString("question")
                    val optsArr = obj.getJSONArray("options")
                    val opts = mutableListOf<String>()
                    for (j in 0 until optsArr.length()) {
                        opts.add(optsArr.getString(j))
                    }
                    val corr = obj.getString("correctAnswer")
                    val explanation = obj.optString("explanation", "The answer '$corr' is confirmed to be the exact match.")
                    
                    list.add(
                        Question(
                            id = i + 1000, // custom generated IDs starts from 1000
                            question = qText,
                            options = opts,
                            correctAnswer = corr,
                            explanation = explanation
                        )
                    )
                }
                
                if (list.size == 5) list else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating dynamic quiz questions: ${e.message}", e)
            null
        }
    }
}
