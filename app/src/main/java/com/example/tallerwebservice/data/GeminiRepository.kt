package com.example.tallerwebservice.data

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio que encapsula la comunicación con el Web Service de Google Gemini AI
 * utilizando el SDK oficial (com.google.ai.client.generativeai).
 */
class GeminiRepository(
    apiKey: String,
    modelName: String = "gemini-3.6-flash"
) {
    private var currentApiKey: String = apiKey
    private var currentModelName: String = modelName
    private var chatSession: Chat? = null

    init {
        if (currentApiKey.isNotBlank() && currentApiKey != "TU_API_KEY_AQUI") {
            initChatSession()
        }
    }

    private fun initChatSession() {
        val generativeModel = GenerativeModel(
            modelName = currentModelName,
            apiKey = currentApiKey,
            systemInstruction = content {
                text("Eres un asistente virtual amable, claro y conciso. Responde siempre en español.")
            }
        )
        chatSession = generativeModel.startChat()
    }

    fun updateApiKey(newApiKey: String) {
        currentApiKey = newApiKey.trim()
        if (currentApiKey.isNotBlank()) {
            initChatSession()
        }
    }

    fun hasValidApiKey(): Boolean {
        return currentApiKey.isNotBlank() && currentApiKey != "TU_API_KEY_AQUI"
    }

    fun resetChat() {
        if (hasValidApiKey()) {
            initChatSession()
        }
    }

    /**
     * Envía un mensaje al modelo de Gemini manteniendo el contexto de la conversación.
     */
    suspend fun sendMessage(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!hasValidApiKey()) {
                return@withContext Result.failure(
                    IllegalStateException("Configura tu API Key de Gemini primero (ícono de llave arriba a la derecha o en gradle.properties).")
                )
            }

            val session = chatSession ?: run {
                initChatSession()
                chatSession!!
            }

            val response = session.sendMessage(prompt)
            val replyText = response.text ?: "No se recibió texto en la respuesta de Gemini."
            Result.success(replyText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
