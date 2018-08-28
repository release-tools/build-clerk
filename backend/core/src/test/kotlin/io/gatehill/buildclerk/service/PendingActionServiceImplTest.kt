package io.gatehill.buildclerk.service

import com.nhaarman.mockitokotlin2.mock
import io.gatehill.buildclerk.api.model.slack.SlackMessageAction
import io.gatehill.buildclerk.api.model.slack.SlackMessageAttachment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for `PendingActionService`.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class PendingActionServiceImplTest {
    private lateinit var service: PendingActionServiceImpl

    private val slackAttachments = listOf(
        SlackMessageAttachment(
            text = "attachment 1 text",
            title = "attachment 1 title",
            actions = emptyList()
        ),
        SlackMessageAttachment(
            text = "attachment 2 text",
            title = "attachment 2 title",
            actions = listOf(
                SlackMessageAction(
                    name = "attachment 2 action 1 name",
                    text = "attachment 2 action 1 text"
                ),
                SlackMessageAction(
                    name = "attachment 2 action 2 name",
                    text = "attachment 2 action 2 text"
                )
            )
        ),
        SlackMessageAttachment(
            text = "attachment 3 text",
            title = "attachment 3 title",
            actions = listOf(
                SlackMessageAction(
                    name = "attachment 3 action 1 name",
                    text = "attachment 3 action 1 text"
                ),
                SlackMessageAction(
                    name = "attachment 3 action 2 name",
                    text = "attachment 3 action 2 text"
                )
            )
        )
    )

    @Before
    fun setUp() {
        service = PendingActionServiceImpl(mock(), mock(), mock(), mock())
    }

    @Test
    fun `compose attachments with no selected actions`() {
        val attachments = service.composeAttachments(
            slackAttachments = slackAttachments,
            selectedActions = emptyList()
        )

        assertEquals(3, attachments.size)

        // no actions
        assertEquals(0, attachments[0].actions?.size)
        assertEquals("attachment 1 text", attachments[0].text)

        // unselected actions
        assertEquals(2, attachments[1].actions?.size)
        assertEquals("attachment 2 action 1 text", attachments[1].actions?.get(0)?.text)
        assertEquals("attachment 2 action 2 text", attachments[1].actions?.get(1)?.text)
        assertEquals(2, attachments[2].actions?.size)
        assertEquals("attachment 3 action 1 text", attachments[2].actions?.get(0)?.text)
        assertEquals("attachment 3 action 2 text", attachments[2].actions?.get(1)?.text)
    }

    @Test
    fun `compose attachments with nonexclusive selected actions`() {
        val selectedActions = listOf(
            SelectedAction(
                actionName = "attachment 2 action 1 name",
                exclusive = false,
                outcomeText = "outcome text"
            )
        )
        val attachments = service.composeAttachments(
            slackAttachments = slackAttachments,
            selectedActions = selectedActions
        )

        assertEquals(3, attachments.size)

        // no actions
        assertEquals(0, attachments[0].actions?.size)
        assertEquals("attachment 1 text", attachments[0].text)

        // selected action
        assertEquals("outcome text", attachments[2].text)
        assertTrue(attachments[2].actions == null)

        // unselected action
        assertEquals(2, attachments[1].actions?.size)
        assertEquals("attachment 3 action 1 text", attachments[1].actions?.get(0)?.text)
        assertEquals("attachment 3 action 2 text", attachments[1].actions?.get(1)?.text)
    }

    @Test
    fun `compose attachments with exclusive selected actions`() {
        val selectedActions = listOf(
            SelectedAction(
                actionName = "attachment 2 action 1 name",
                exclusive = true,
                outcomeText = "outcome text"
            )
        )
        val attachments = service.composeAttachments(
            slackAttachments = slackAttachments,
            selectedActions = selectedActions
        )

        assertEquals(2, attachments.size)

        // no actions
        assertEquals(0, attachments[0].actions?.size)
        assertEquals("attachment 1 text", attachments[0].text)

        // selected action
        assertEquals("outcome text", attachments[1].text)
        assertTrue(attachments[1].actions == null)
    }
}
