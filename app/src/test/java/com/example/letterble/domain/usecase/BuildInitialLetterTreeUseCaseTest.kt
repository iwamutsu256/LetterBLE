package com.example.letterble.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildInitialLetterTreeUseCaseTest {

    @Test
    fun `creates root node for sender without edges`() {
        val useCase = BuildInitialLetterTreeUseCase()

        val tree = useCase(
            fromUser = "sender",
            latitude = 35.681236,
            longitude = 139.767125
        )

        assertEquals(1, tree.nodes.size)
        assertEquals("sender", tree.nodes.single().id)
        assertEquals("sender", tree.nodes.single().userName)
        assertEquals(35.681236, tree.nodes.single().latitude, 0.0)
        assertEquals(139.767125, tree.nodes.single().longitude, 0.0)
        assertTrue(tree.edges.isEmpty())
    }
}
