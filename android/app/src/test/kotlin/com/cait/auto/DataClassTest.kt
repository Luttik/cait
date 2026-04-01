package com.cait.auto

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DataClassTest {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Test
    fun `AgentEvent holds type and data`() {
        val event = AgentEvent(type = "TEXT_MESSAGE_CONTENT", data = "Hello!")
        assertEquals("TEXT_MESSAGE_CONTENT", event.type)
        assertEquals("Hello!", event.data)
    }

    @Test
    fun `RunAgentPayload serializes to JSON`() {
        val payload = RunAgentPayload(
            threadId = "t1",
            runId = "r1",
            messages = listOf(AgentMessage(role = "user", content = "hi"))
        )
        val adapter = moshi.adapter(RunAgentPayload::class.java)
        val json = adapter.toJson(payload)

        assertNotNull(json)
        assert(json.contains("\"threadId\":\"t1\""))
        assert(json.contains("\"runId\":\"r1\""))
        assert(json.contains("\"role\":\"user\""))
        assert(json.contains("\"content\":\"hi\""))
    }

    @Test
    fun `AgentMessage holds role and content`() {
        val msg = AgentMessage(role = "assistant", content = "I can help!")
        assertEquals("assistant", msg.role)
        assertEquals("I can help!", msg.content)
    }
}
