package com.example.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CoachRole(val displayName: String, val systemPrompt: String) {
    HABIT_ARCHITECT(
        displayName = "Habit Architect",
        systemPrompt = "You are HabitPulse AI Coach, a world-class behavioral scientist and habit architect. You guide users to design atomic, friction-free daily habits (Health, Work, Study, Fitness). Break big ambitious dreams into 2-minute actionable rituals. Keep answers encouraging, structured, bulleted, and actionable on mobile."
    ),
    DISCIPLINE_MENTOR(
        displayName = "Discipline Mentor",
        systemPrompt = "You are HabitPulse Discipline Mentor. You offer firm, pragmatic, no-excuses encouragement inspired by Marcus Aurelius and elite performance psychologists. Emphasize consistency over intensity, identity-based habits, and doing hard things daily. Keep advice concise and inspiring."
    ),
    STREAK_RESCUER(
        displayName = "Streak Recovery",
        systemPrompt = "You are HabitPulse Streak Recovery Specialist. When users break a streak or feel demotivated, you offer empathy without pity. Teach them the 'never miss twice' rule, how to reset their dopamine baseline, and how to restart momentum right now. Keep advice warm, practical, and uplifting."
    )
}

class HabitCoachViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "model",
                text = "Hello! I'm your HabitPulse AI Coach. 🎯 What habit would you like to build, optimize, or troubleshoot today (Health, Work, Study, etc.)?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedRole = MutableStateFlow(CoachRole.HABIT_ARCHITECT)
    val selectedRole: StateFlow<CoachRole> = _selectedRole.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.8-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private var chatSession = initChatSession()

    private fun initChatSession() = try {
        val model = Firebase.ai.generativeModel(
            modelName = _selectedModel.value,
            systemInstruction = content { text(_selectedRole.value.systemPrompt) }
        )
        model.startChat()
    } catch (e: Exception) {
        Log.e("HabitCoach", "Failed to init generativeModel", e)
        null
    }

    fun setRole(role: CoachRole) {
        _selectedRole.value = role
        chatSession = initChatSession()
        _messages.value = _messages.value + ChatMessage(
            sender = "model",
            text = "Switched persona to ${role.displayName}. How can I assist you now?"
        )
    }

    fun setModel(modelName: String) {
        _selectedModel.value = modelName
        chatSession = initChatSession()
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        val userMessage = ChatMessage(sender = "user", text = trimmed)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                var session = chatSession
                if (session == null) {
                    session = initChatSession()
                    chatSession = session
                }

                val response = withTimeout(60_000L) {
                    session?.sendMessage(trimmed)
                }

                val replyText = response?.text?.trim() ?: "I'm reflecting on your habit strategy. Could you rephrase your question?"
                _messages.value = _messages.value + ChatMessage(
                    sender = "model",
                    text = replyText
                )
            } catch (e: TimeoutCancellationException) {
                Log.e("HabitCoach", "AI generation timed out", e)
                _messages.value = _messages.value + ChatMessage(
                    sender = "model",
                    text = "Request timed out after 60 seconds. Please try again!"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HabitCoach", "AI generation error", e)
                _messages.value = _messages.value + ChatMessage(
                    sender = "model",
                    text = "Unable to reach HabitPulse AI Coach (${e.localizedMessage ?: "Network error"}). Please check your connection."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        chatSession = initChatSession()
        _messages.value = listOf(
            ChatMessage(
                sender = "model",
                text = "Chat history cleared. What habit goals should we tackle next?"
            )
        )
    }
}
