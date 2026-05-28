package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SubmitLetterUseCaseTest {

    @Test
    fun `saves letter with sender root tree and posting location`() = runBlocking {
        val letterRepository = FakeSubmitLetterRepository()
        val useCase = SubmitLetterUseCase(
            letterRepository = letterRepository,
            currentTimeMillis = { 1000L },
            letterIdFactory = { "letter-1" }
        )

        val result = useCase.execute(
            SubmitLetterCommand(
                fromUser = " sender ",
                toUser = " receiver ",
                sentence = " hello ",
                latitude = 35.0,
                longitude = 139.0
            )
        )

        assertEquals("letter-1", result.letterId)
        assertEquals("letter-1_posted_1000", result.locationId)
        assertEquals("receiver", letterRepository.savedLetter?.toUser)
        assertEquals("sender", letterRepository.savedLetter?.fromUser)
        assertEquals("hello", letterRepository.savedLetter?.sentence)
        assertEquals("sender", letterRepository.savedLetter?.tree?.nodes?.single()?.userName)
        assertEquals("letter-1", letterRepository.savedLocation?.letterId)
        assertEquals("sender", letterRepository.savedLocation?.userName)
    }

    @Test
    fun `rejects letter addressed to sender`() = runBlocking {
        val letterRepository = FakeSubmitLetterRepository()
        val useCase = SubmitLetterUseCase(
            letterRepository = letterRepository,
            currentTimeMillis = { 1000L },
            letterIdFactory = { "letter-1" }
        )

        try {
            useCase.execute(
                SubmitLetterCommand(
                    fromUser = "me",
                    toUser = " me ",
                    sentence = "hello",
                    latitude = 35.0,
                    longitude = 139.0
                )
            )
            fail("自分宛て投函は失敗する必要があります")
        } catch (exception: IllegalArgumentException) {
            assertEquals("cannot submit a letter to yourself", exception.message)
        }

        assertEquals(null, letterRepository.savedLetter)
        assertEquals(null, letterRepository.savedLocation)
    }
}

private class FakeSubmitLetterRepository : SubmitLetterRepository {
    var savedLetter: Letter? = null
    var savedLocation: Location? = null

    override suspend fun submitLetter(letter: Letter, initialLocation: Location) {
        savedLetter = letter
        savedLocation = initialLocation
    }
}
