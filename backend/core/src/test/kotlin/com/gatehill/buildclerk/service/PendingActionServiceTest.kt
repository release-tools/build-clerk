package com.gatehill.buildclerk.service

import com.gatehill.buildclerk.model.slack.SlackAttachmentAction
import com.gatehill.buildclerk.model.slack.SlackMessageAttachment
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for `PendingActionService`.
 *
 * @author pete
 */
class PendingActionServiceTest {
    private lateinit var service: PendingActionService

    @Before
    fun setUp() {
        service = PendingActionService(mock(), mock(), mock())
    }

    @Test
    fun `compose attachments with no selected actions`() {
        val slackAttachments = listOf(
            SlackMessageAttachment(
                text = "attachment text",
                title = "attachment title",
                actions = listOf(
                    SlackAttachmentAction(
                        name = "action name",
                        text = "action text"
                    )
                )
            )
        )
        val attachments = service.composeAttachments(
            slackAttachments = slackAttachments,
            selectedActions = emptyList(),
            exclusiveActionExecuted = false
        )

        assertEquals(1, attachments.size)
        assertEquals("attachment text", attachments[0].text)

        // action unresolved
        assertEquals(1, attachments[0].actions?.size)
        assertEquals("action text", attachments[0].actions?.get(0)?.text)
    }

    @Test
    fun `compose attachments with nonexclusive selected actions`() {
        val slackAttachments = listOf(
            SlackMessageAttachment(
                text = "attachment text",
                title = "attachment title",
                actions = listOf(
                    SlackAttachmentAction(
                        name = "action 1 name",
                        text = "action 1 text"
                    ),
                    SlackAttachmentAction(
                        name = "action 2 name",
                        text = "action 2 text"
                    )
                )
            )
        )
        val selectedActions = listOf(
            SelectedAction(
                actionName = "action 1 name",
                outcomeText = "outcome text"
            )
        )
        val attachments = service.composeAttachments(
            slackAttachments = slackAttachments,
            selectedActions = selectedActions,
            exclusiveActionExecuted = false
        )

        assertEquals(2, attachments.size)
        assertEquals("attachment text", attachments[0].text)

        // action unresolved
        assertEquals(1, attachments[0].actions?.size)
        assertEquals("action 2 text", attachments[0].actions?.get(0)?.text)

        // action resolved
        assertEquals("outcome text", attachments[1].text)
        assertTrue(attachments[1].actions == null)
    }

    @Test
    fun `compose attachments with exclusive selected actions`() {
        val slackAttachments = listOf(
            SlackMessageAttachment(
                text = "attachment text",
                title = "attachment title",
                actions = listOf(
                    SlackAttachmentAction(
                        name = "action 1 name",
                        text = "action 1 text"
                    ),
                    SlackAttachmentAction(
                        name = "action 2 name",
                        text = "action 2 text"
                    )
                )
            )
        )
        val selectedActions = listOf(
            SelectedAction(
                actionName = "action 1 name",
                outcomeText = "outcome text"
            )
        )
        val attachments = service.composeAttachments(
            slackAttachments = slackAttachments,
            selectedActions = selectedActions,
            exclusiveActionExecuted = true
        )

        assertEquals(2, attachments.size)
        assertEquals("attachment text", attachments[0].text)
        assertTrue(attachments[0].actions?.isEmpty() == true)

        // action resolved
        assertEquals("outcome text", attachments[1].text)
        assertTrue(attachments[1].actions == null)
    }
}
