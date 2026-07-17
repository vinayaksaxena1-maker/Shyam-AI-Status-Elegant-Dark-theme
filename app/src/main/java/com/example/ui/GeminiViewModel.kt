package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiRepository
import com.example.data.GeminiRepositoryImpl
import com.example.data.GeminiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GeminiUiState {
    object Idle : GeminiUiState()
    object Loading : GeminiUiState()
    data class Success(val responseText: String) : GeminiUiState()
    data class Error(val errorMessage: String) : GeminiUiState()
}

class GeminiViewModel(
    private val repository: GeminiRepository = GeminiRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val uiState: StateFlow<GeminiUiState> = _uiState

    var currentPromptInput by mutableStateOf("")
    var currentStyleSelection by mutableStateOf("Bhakti Style")

    /**
     * Generates general creative devotional text.
     */
    fun generateDevotionalText(prompt: String, systemInstruction: String? = null) {
        if (prompt.isBlank()) return
        _uiState.value = GeminiUiState.Loading

        viewModelScope.launch {
            when (val result = repository.generateDevotionalText(prompt, systemInstruction)) {
                is GeminiResult.Success -> {
                    _uiState.value = GeminiUiState.Success(result.data)
                }
                is GeminiResult.Error -> {
                    _uiState.value = GeminiUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Enhances a basic user prompt and triggers a callback on successful completion.
     */
    fun enhancePromptAndGenerate(inputPrompt: String, style: String, onFinished: (String) -> Unit) {
        if (inputPrompt.isBlank()) return
        _uiState.value = GeminiUiState.Loading

        viewModelScope.launch {
            when (val result = repository.enhanceDevotionalPrompt(inputPrompt, style)) {
                is GeminiResult.Success -> {
                    _uiState.value = GeminiUiState.Success(result.data)
                    onFinished(result.data)
                }
                is GeminiResult.Error -> {
                    _uiState.value = GeminiUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Resets the UI State to Idle.
     */
    fun resetState() {
        _uiState.value = GeminiUiState.Idle
    }
}
