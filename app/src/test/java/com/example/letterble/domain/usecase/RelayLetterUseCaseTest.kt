package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Encounter
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RelayLetterUseCaseTest {

    @Test
    fun `duplicate encounter in the same direction does not relay letters`() = runBlocking {
        val repositories = FakeRelayRepositories(
            lastEncounters = mapOf(
                ("me" to "target") to Encounter(
                    encounterId = "encounter-1",
                    userA = "me",
                    userB = "target",
                    timestamp = 900L
                )
            ),
            carriedLetters = listOf(letter(id = "letter-1"))
        )
        val useCase = repositories.useCase(now = 1_000L)

        useCase.execute(myUserName = "me", targetUserName = "target")

        assertEquals(emptyList<Encounter>(), repositories.savedEncounters)
        assertEquals(emptyList<Pair<String, List<String>>>(), repositories.addedCarryingLetterIds)
        assertEquals(emptyList<Location>(), repositories.savedLocations)
        assertEquals(emptyList<TreeAddNodeCall>(), repositories.addNodeCalls)
        assertEquals(emptyList<String>(), repositories.operations)
        assertEquals(emptyList<Pair<String, Boolean>>(), repositories.updatedSurvivals)
    }

    @Test
    fun `encounter in the reverse direction does not suppress relay`() = runBlocking {
        val repositories = FakeRelayRepositories(
            lastEncounters = mapOf(
                ("target" to "me") to Encounter(
                    encounterId = "encounter-1",
                    userA = "target",
                    userB = "me",
                    timestamp = 900L
                )
            ),
            carriedLetters = listOf(letter(id = "letter-1"))
        )
        val useCase = repositories.useCase(now = 1_000L)

        useCase.execute(myUserName = "me", targetUserName = "target")

        assertEquals(listOf("letter-1"), repositories.addedCarryingLetterIds.single().second)
        assertEquals(1, repositories.savedLocations.size)
        assertEquals(1, repositories.addNodeCalls.size)
        assertEquals(1, repositories.savedEncounters.size)
    }

    @Test
    fun `letter already containing me in tree is not relayed or recorded`() = runBlocking {
        val repositories = FakeRelayRepositories(
            carriedLetters = listOf(
                letter(
                    id = "letter-1",
                    tree = Tree(nodes = listOf(Node(id = "me", userName = "me")))
                )
            )
        )
        val useCase = repositories.useCase(now = 1_000L)

        useCase.execute(myUserName = "me", targetUserName = "target")

        assertEquals(emptyList<Encounter>(), repositories.savedEncounters)
        assertEquals(emptyList<Pair<String, List<String>>>(), repositories.addedCarryingLetterIds)
        assertEquals(emptyList<Location>(), repositories.savedLocations)
        assertEquals(emptyList<TreeAddNodeCall>(), repositories.addNodeCalls)
        assertEquals(emptyList<String>(), repositories.operations)
    }

    @Test
    fun `tree transaction duplicate does not save location carrying or encounter`() = runBlocking {
        val repositories = FakeRelayRepositories(
            carriedLetters = listOf(letter(id = "letter-1")),
            addNodeResults = mutableListOf(false)
        )
        val useCase = repositories.useCase(now = 1_000L)

        useCase.execute(myUserName = "me", targetUserName = "target")

        assertEquals(emptyList<Encounter>(), repositories.savedEncounters)
        assertEquals(emptyList<Pair<String, List<String>>>(), repositories.addedCarryingLetterIds)
        assertEquals(emptyList<Location>(), repositories.savedLocations)
        assertEquals(1, repositories.addNodeCalls.size)
        assertEquals(listOf("addNode:letter-1"), repositories.operations)
    }

    @Test
    fun `letter addressed to me is marked as delivered`() = runBlocking {
        val repositories = FakeRelayRepositories(
            carriedLetters = listOf(letter(id = "letter-1", toUser = "me"))
        )
        val useCase = repositories.useCase(
            now = 1_000L,
            coordinates = RelayCoordinates(latitude = 35.0, longitude = 139.0)
        )

        useCase.execute(myUserName = "me", targetUserName = "target")

        assertEquals(listOf("letter-1"), repositories.addedCarryingLetterIds.single().second)
        assertEquals("letter-1", repositories.savedLocations.single().letterId)
        assertEquals("me", repositories.savedLocations.single().userName)
        assertEquals(35.0, repositories.savedLocations.single().latitude, 0.0)
        assertEquals(139.0, repositories.savedLocations.single().longitude, 0.0)
        assertEquals(
            TreeAddNodeCall(
                letterId = "letter-1",
                parentUser = "target",
                newUser = "me",
                location = repositories.savedLocations.single()
            ),
            repositories.addNodeCalls.single()
        )
        assertEquals(listOf("letter-1" to false), repositories.updatedSurvivals)
        assertEquals(1, repositories.savedEncounters.size)
        assertEquals(
            listOf(
                "addNode:letter-1",
                "saveLocation:letter-1",
                "updateSurvival:letter-1",
                "addCarryingLetterIds:letter-1",
                "saveEncounter"
            ),
            repositories.operations
        )
    }

    private fun FakeRelayRepositories.useCase(
        now: Long,
        coordinates: RelayCoordinates = RelayCoordinates(latitude = 35.0, longitude = 139.0)
    ): RelayLetterUseCase {
        return RelayLetterUseCase(
            encounterRepository = this,
            letterRepository = this,
            locationRepository = this,
            treeRepository = this,
            userRepository = this,
            currentTimeMillis = { now },
            currentCoordinates = { coordinates }
        )
    }

    private fun letter(
        id: String,
        toUser: String = "receiver",
        tree: Tree = Tree(nodes = listOf(Node(id = "target", userName = "target")))
    ): Letter {
        return Letter(
            letterId = id,
            toUser = toUser,
            fromUser = "sender",
            sentence = "hello",
            isSurvival = true,
            tree = tree
        )
    }
}

private data class TreeAddNodeCall(
    val letterId: String,
    val parentUser: String,
    val newUser: String,
    val location: Location
)

private class FakeRelayRepositories(
    private val lastEncounters: Map<Pair<String, String>, Encounter> = emptyMap(),
    private val carriedLetters: List<Letter> = emptyList(),
    private val addNodeResults: MutableList<Boolean> = mutableListOf(true)
) : RelayEncounterRepository,
    RelayLetterRepository,
    RelayLocationRepository,
    RelayTreeRepository,
    RelayUserRepository {

    val savedEncounters = mutableListOf<Encounter>()
    val addedCarryingLetterIds = mutableListOf<Pair<String, List<String>>>()
    val savedLocations = mutableListOf<Location>()
    val addNodeCalls = mutableListOf<TreeAddNodeCall>()
    val updatedSurvivals = mutableListOf<Pair<String, Boolean>>()
    val operations = mutableListOf<String>()

    override suspend fun saveEncounter(encounter: Encounter) {
        operations += "saveEncounter"
        savedEncounters += encounter
    }

    override suspend fun getLastEncounter(userA: String, userB: String): Encounter? {
        return lastEncounters[userA to userB]
    }

    override suspend fun getCarriedLetters(userName: String): List<Letter> {
        return carriedLetters
    }

    override suspend fun updateSurvival(letterId: String, isSurvival: Boolean) {
        operations += "updateSurvival:$letterId"
        updatedSurvivals += letterId to isSurvival
    }

    override suspend fun saveLocation(location: Location) {
        operations += "saveLocation:${location.letterId}"
        savedLocations += location
    }

    override suspend fun addNode(
        letterId: String,
        parentUser: String,
        newUser: String,
        location: Location
    ): Boolean {
        operations += "addNode:$letterId"
        addNodeCalls += TreeAddNodeCall(
            letterId = letterId,
            parentUser = parentUser,
            newUser = newUser,
            location = location
        )
        return if (addNodeResults.isNotEmpty()) {
            addNodeResults.removeAt(0)
        } else {
            true
        }
    }

    override suspend fun addCarryingLetterIds(userName: String, letterIds: List<String>) {
        operations += "addCarryingLetterIds:${letterIds.joinToString(",")}"
        addedCarryingLetterIds += userName to letterIds
    }
}
