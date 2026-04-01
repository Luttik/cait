package com.cait.auto

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Lightweight SSE client that talks to the Cait backend AG-UI endpoint.
 */
class AgentClient(
    private val baseUrl: String = BuildConfig.BACKEND_URL
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Send a user message to the agent and receive a [Flow] of SSE events.
     *
     * Each emitted [AgentEvent] contains the raw `type` and `data` from the
     * server-sent event stream.
     */
    fun chat(
        userMessage: String,
        threadId: String = UUID.randomUUID().toString()
    ): Flow<AgentEvent> = callbackFlow {
        val runId = UUID.randomUUID().toString()

        val payload = RunAgentPayload(
            threadId = threadId,
            runId = runId,
            messages = listOf(
                AgentMessage(role = "user", content = userMessage)
            )
        )

        val adapter = moshi.adapter(RunAgentPayload::class.java)
        val json = adapter.toJson(payload)

        val request = Request.Builder()
            .url("$baseUrl/agent")
            .post(json.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(client)

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                trySend(AgentEvent(type = type ?: "message", data = data))
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                close(t ?: Exception("SSE stream failed"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        factory.newEventSource(request, listener)

        awaitClose { client.dispatcher.cancelAll() }
    }

    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}

data class AgentEvent(
    val type: String,
    val data: String
)

@JsonClass(generateAdapter = false)
data class RunAgentPayload(
    val threadId: String,
    val runId: String,
    val messages: List<AgentMessage>
)

@JsonClass(generateAdapter = false)
data class AgentMessage(
    val role: String,
    val content: String
)
