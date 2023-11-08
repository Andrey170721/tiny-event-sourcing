package ru.quipy

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.*
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserAggregateStateTest {

    @Autowired
    private lateinit var userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    private val testUserId = UUID.randomUUID()
    private val testUsername = "TestUser"
    private val testNickname = "TestNick"
    private val testPassword = "TestPassword"
    private val newUsername = "NewTestUser"

    @BeforeEach
    fun init() {
        // Remove existing data
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(testUserId)), "aggregate-user")
    }

    @Test
    fun addUserTest() {
        val createdEvent: UserCreatedEvent = userEsService.create {
            it.create(testUserId, testUsername, testNickname, testPassword)
        }

        Assertions.assertEquals(testUserId, createdEvent.userId)
        Assertions.assertEquals(testUsername, createdEvent.firstname)
        Assertions.assertEquals(testNickname, createdEvent.nickname)
        Assertions.assertEquals(testPassword, createdEvent.password)
    }

    @Test
    fun getUserTest() {
        userEsService.create {
            it.create(testUserId, testUsername, testNickname, testPassword)
        }

        val userState = userEsService.getState(testUserId)
        Assertions.assertNotNull(userState)
        Assertions.assertEquals(testUserId, userState?.getId())
        Assertions.assertEquals(testUsername, userState?.name)
        Assertions.assertEquals(testNickname, userState?.nickname)
    }

    @Test
    fun changeUserNameTest() {
        userEsService.create {
            it.create(testUserId, testUsername, testNickname, testPassword)
        }

        val changedNameEvent: UserChangedNameEvent = userEsService.update(testUserId) {
            it.changeName(newUsername)
        }

        val userState = userEsService.getState(testUserId)
        Assertions.assertNotNull(userState)
        Assertions.assertEquals(newUsername, userState?.name)
    }
}
