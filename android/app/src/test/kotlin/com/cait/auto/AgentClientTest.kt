package com.cait.auto

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AgentClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: AgentClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = AgentClient(baseUrl = server.url("/").toString().trimEnd('/'))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `healthCheck returns true on 200`() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":"ok"}""").setResponseCode(200))
        assertTrue(client.healthCheck())
    }

    @Test
    fun `healthCheck returns false on 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        assertTrue(!client.healthCheck())
    }

    @Test
    fun `chat sends correct request body`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody("event: RUN_FINISHED\ndata: done\n\n")
        )

        try {
            client.chat("hello", threadId = "test-thread").toList()
        } catch (_: Exception) {
            // SSE parsing may throw; we only care about the request
        }

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.endsWith("/agent"))

        val body = request.body.readUtf8()
        assertTrue(body.contains("\"content\":\"hello\""))
        assertTrue(body.contains("\"threadId\":\"test-thread\""))
    }
}
