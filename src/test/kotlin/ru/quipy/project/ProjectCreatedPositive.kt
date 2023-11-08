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
class ProjectCreatedPositive {

    @LocalServerPort
    private val port: Int = 8080

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun testCreateProject() {
        val projectTitle = "Test Project"
        val creatorId = UUID.randomUUID()

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем, что запрос вернул статус 200 OK
        Assertions.assertEquals(response.statusCode, HttpStatus.OK)

        // Проверяем, что ответ содержит созданный проект
        val projectCreatedEvent = response.body
        Assertions.assertNotNull(projectCreatedEvent)
        Assertions.assertNotNull(projectCreatedEvent?.projectId)
        Assertions.assertEquals(projectCreatedEvent?.title, projectTitle)
        Assertions.assertEquals(projectCreatedEvent?.creatorId, creatorId)
    }
}
