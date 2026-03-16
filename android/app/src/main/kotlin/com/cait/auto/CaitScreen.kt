package com.cait.auto

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Header
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Main Cait screen shown in Android Auto.
 *
 * Displays a single "Talk to Cait" button.  When pressed it launches the
 * platform speech recogniser, sends the transcript to the backend, and
 * streams back the agent's response.  If the agent proposes a tool call
 * the screen pushes [ApprovalScreen] for driver confirmation.
 */
class CaitScreen(carContext: CarContext) : Screen(carContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val agentClient = AgentClient()

    private var statusText: String = "Press the button to talk to Cait"
    private var agentReply: String? = null

    override fun onGetTemplate(): Template {
        val displayText = agentReply ?: statusText

        return MessageTemplate.Builder(displayText)
            .setHeader(Header.Builder().setTitle("Cait").build())
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, R.drawable.ic_launcher)
                ).build()
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.voice_button_label))
                    .setOnClickListener { startListening() }
                    .build()
            )
            .build()
    }

    private fun startListening() {
        statusText = carContext.getString(R.string.listening_label)
        agentReply = null
        invalidate()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }

        carContext.startActivity(intent)
    }

    /**
     * Called after the speech recogniser returns a transcript.
     */
    fun onSpeechResult(transcript: String) {
        statusText = carContext.getString(R.string.thinking_label)
        agentReply = null
        invalidate()

        scope.launch {
            val builder = StringBuilder()
            agentClient.chat(transcript)
                .catch { e ->
                    agentReply = "Error: ${e.message}"
                    invalidate()
                }
                .collect { event ->
                    when (event.type) {
                        "TEXT_MESSAGE_CONTENT" -> {
                            builder.append(event.data)
                            agentReply = builder.toString()
                            invalidate()
                        }

                        "TOOL_CALL_START" -> {
                            screenManager.push(
                                ApprovalScreen(
                                    carContext,
                                    toolCallData = event.data
                                ) { approved ->
                                    onToolApprovalResult(approved)
                                }
                            )
                        }

                        "RUN_FINISHED" -> {
                            if (agentReply.isNullOrBlank()) {
                                agentReply = "Done."
                            }
                            invalidate()
                        }
                    }
                }
        }
    }

    private fun onToolApprovalResult(approved: Boolean) {
        if (approved) {
            statusText = "Executing…"
        } else {
            statusText = "Action cancelled."
            agentReply = null
        }
        invalidate()
    }

    init {
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                scope.cancel()
            }
        })
    }
}
