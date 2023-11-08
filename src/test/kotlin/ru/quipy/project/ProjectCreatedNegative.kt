package ru.quipy.project

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import ru.quipy.api.ProjectCreatedEvent
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectCreatedNegative {

    @LocalServerPort
    private val port: Int = 8080

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun testCreateProject() {
        val projectTitle = "Test Project"
        val creatorId = null

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем, что запрос вернул статус 200 OK
        Assertions.assertNotEquals(response.statusCode, HttpStatus.OK)

    }
}