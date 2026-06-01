package com.example.letterble.feature.carry

import com.example.letterble.domain.model.Letter
import org.junit.Assert.assertEquals
import org.junit.Test

class CarryViewModelTest {
    @Test
    fun `carry list includes delivered letters unless they are addressed to current user`() {
        val letters = listOf(
            letter(id = "relaying", toUser = "receiver", fromUser = "sender", isSurvival = true),
            letter(id = "delivered-other", toUser = "receiver", fromUser = "sender", isSurvival = false),
            letter(id = "received", toUser = "me", fromUser = "sender", isSurvival = false),
            letter(id = "own", toUser = "receiver", fromUser = "me", isSurvival = true)
        )

        val items = letters.toCarryListItemsForUser("me")

        assertEquals(listOf("relaying", "delivered-other"), items.map { it.letterId })
        assertEquals(listOf(true, false), items.map { it.isSurvival })
    }

    private fun letter(
        id: String,
        toUser: String,
        fromUser: String,
        isSurvival: Boolean
    ): Letter {
        return Letter(
            letterId = id,
            toUser = toUser,
            fromUser = fromUser,
            isSurvival = isSurvival
        )
    }
}
