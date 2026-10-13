package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CivicRepository
import com.example.model.MainNavigationTab
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiCopilotRobolectricTest {

    @Test
    fun `navigation destinations include both AI Copilot and Notifications independently`() {
        val tabs = MainNavigationTab.values().toList()
        assertTrue("ASSISTANT tab must exist", tabs.contains(MainNavigationTab.ASSISTANT))
        assertTrue("NOTIFICATIONS tab must exist", tabs.contains(MainNavigationTab.NOTIFICATIONS))
        assertEquals("AI Copilot", MainNavigationTab.ASSISTANT.label)
        assertEquals("Notifications", MainNavigationTab.NOTIFICATIONS.label)
        assertNotEquals(MainNavigationTab.ASSISTANT, MainNavigationTab.NOTIFICATIONS)
    }

    @Test
    fun `blank chat message submission is rejected`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CivicRepository(context)

        val initialCount = repository.chatMessages.value.size
        val result = repository.sendChatMessage("   ")

        assertTrue("Blank message submission must fail", result.isFailure)
        assertEquals("Chat message list must remain unchanged on blank input", initialCount, repository.chatMessages.value.size)
    }

    @Test
    fun `clearChat resets conversation history to initial welcome message`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CivicRepository(context)

        repository.provideOfflineGuidance("How to report a pothole")
        assertTrue(repository.chatMessages.value.size > 1)

        repository.clearChat()
        assertEquals(1, repository.chatMessages.value.size)
        assertFalse(repository.chatMessages.value.first().isUser)
    }

    @Test
    fun `offline civic guidance provides explicitly labeled municipal handbook information`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CivicRepository(context)

        repository.provideOfflineGuidance("How to report a broken streetlight")

        val lastMessage = repository.chatMessages.value.last()
        assertTrue("Message must be flagged as offline guidance", lastMessage.isOfflineGuidance)
        assertFalse("Message must not be flagged as user", lastMessage.isUser)
        assertTrue("Content must contain official guidelines", lastMessage.text.contains("Official Municipal Guide", ignoreCase = true))
        assertTrue("Must provide streetlight advice", lastMessage.text.contains("Streetlight", ignoreCase = true))
    }

    @Test
    fun `offline emergency helplines query provides verified numbers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CivicRepository(context)

        repository.provideOfflineGuidance("emergency helpline")

        val lastMessage = repository.chatMessages.value.last()
        assertTrue(lastMessage.isOfflineGuidance)
        assertTrue(lastMessage.text.contains("112"))
        assertTrue(lastMessage.text.contains("108"))
        assertTrue(lastMessage.text.contains("101"))
    }
}
