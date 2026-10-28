package com.example.tallerwebservice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallerwebservice.BuildConfig
import com.example.tallerwebservice.data.GeminiRepository
import com.example.tallerwebservice.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "¡Hola! Soy tu chatbot con IA impulsado por el Web Service de Google Gemini. ¿En qué te puedo ayudar hoy?",
            isFromUser = false
        )
    ),
    val isLoading: Boolean = false,
    val isApiKeyConfigured: Boolean = false
)

class ChatViewModel(
    private val repository: GeminiRepository = GeminiRepository(
        apiKey = BuildConfig.GEMINI_API_KEY
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(isApiKeyConfigured = repository.hasValidApiKey())
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            text = trimmed,
            isFromUser = true
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = repository.sendMessage(trimmed)

            result.fold(
                onSuccess = { reply ->
                    val botMessage = ChatMessage(
                        text = reply,
                        isFromUser = false
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + botMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage(
                        text = "Error al consultar Gemini: ${error.localizedMessage ?: "Error desconocido"}",
                        isFromUser = false,
                        isError = true
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + errorMessage,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun setApiKey(apiKey: String) {
        repository.updateApiKey(apiKey)
        _uiState.update { state ->
            state.copy(isApiKeyConfigured = repository.hasValidApiKey())
        }
    }

    fun clearChat() {
        repository.resetChat()
        _uiState.update { state ->
            state.copy(
                messages = listOf(
                    ChatMessage(
                        text = "Conversación reiniciada. ¿En qué te puedo ayudar?",
                        isFromUser = false
                    )
                ),
                isLoading = false
            )
        }
    }
}