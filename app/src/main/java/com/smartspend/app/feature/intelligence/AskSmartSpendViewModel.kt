package com.smartspend.app.feature.intelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartspend.app.core.datastore.PreferencesManager
import com.smartspend.app.domain.intelligence.AiChatMessage
import com.smartspend.app.domain.intelligence.AskSmartSpendEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AskSmartSpendUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val isThinking: Boolean = false,
    val suggestedChips: List<String> = listOf(
        "How much can I spend today?",
        "Can I afford dinner for ₹1500?",
        "What is my health score?",
        "Check my spending leaks",
        "Maza ya mahinyat kiti kharch jhala?"
    )
)

@HiltViewModel
class AskSmartSpendViewModel @Inject constructor(
    private val aiEngine: AskSmartSpendEngine,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskSmartSpendUiState())
    val uiState: StateFlow<AskSmartSpendUiState> = _uiState.asStateFlow()

    private var activeProfileId: String? = null

    init {
        viewModelScope.launch {
            val profileId = preferencesManager.activeProfileIdFlow.firstOrNull() ?: return@launch
            activeProfileId = profileId

            // Initial AI Welcome Greeting
            val welcome = aiEngine.answerQuestion(profileId, "help")
            _uiState.value = _uiState.value.copy(
                messages = listOf(welcome),
                suggestedChips = welcome.quickReplies.ifEmpty { _uiState.value.suggestedChips }
            )
        }
    }

    fun sendMessage(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val profileId = activeProfileId ?: return
        val userMsg = AiChatMessage(isUser = true, text = trimmed)

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            isThinking = true
        )

        viewModelScope.launch {
            val aiResponse = aiEngine.answerQuestion(profileId, trimmed)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiResponse,
                isThinking = false,
                suggestedChips = aiResponse.quickReplies.ifEmpty { _uiState.value.suggestedChips }
            )
        }
    }
}
