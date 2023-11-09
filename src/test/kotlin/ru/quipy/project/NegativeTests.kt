package ru.quipy.project

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import ru.quipy.api.*
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NegativeTests {

    @LocalServerPort
    private val port: Int = 8080

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun testCreateProject() {
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val projectTitle = "Test Project1"

        val response = restTemplate.postForEntity(
                "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
                null,
                ProjectCreatedEvent::class.java
        )

        var projectCreatedEvent = response.body

        val projectTitle2 = "Test Project2"

        val response2 = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle2?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        var projectCreatedEvent2 = response2.body

        Assertions.assertNotEquals(projectCreatedEvent?.projectId, projectCreatedEvent2?.projectId)
    }

    @Test
    fun testAddUserToTaskNotFromProject() {
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val userName1 = UUID.randomUUID().toString()
        val nickname1 = UUID.randomUUID().toString()
        val password1 = UUID.randomUUID().toString()

        val responseUser2 = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName1?nickname=$nickname1&password=$password1",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val projectTitle = "Test Project1"

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )
        var projectId = response.body?.projectId

        var createdUserId1 = responseUser2.body?.userId

        val taskTitle = "New Task"

        val createTaskResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/tasks/$taskTitle?actorId=$creatorId",
            null,
            TaskCreatedEvent::class.java
        )

        val taskId = createTaskResponse.body?.taskId

        // Отправляем запрос на назначение пользователя на задачу
        try {
            val assignUserToTaskResponse = restTemplate.postForEntity(
                "http://localhost:$port/projects/$projectId/tasks/$taskId/assign/$createdUserId1?actorId=$creatorId",
                null,
                MemberAssignedToTaskEvent::class.java
            )
        } catch(e : Exception) {
        }



    }

    @Test
    fun testAddUserThatAlreadyInProject(){
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val userName1 = UUID.randomUUID().toString()
        val nickname1 = UUID.randomUUID().toString()
        val password1 = UUID.randomUUID().toString()

        val responseUser2 = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName1?nickname=$nickname1&password=$password1",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val projectTitle = "Test Project1"

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )
        var projectId = response.body?.projectId

        var createdUserId1 = responseUser2.body?.userId


        // Отправляем запрос на назначение пользователя на задачу
        try {
            val assignUserToProject = restTemplate.postForEntity(
                "http://localhost:$port/projects/$projectId/user/$createdUserId1?actorId=$creatorId",
                null,
                MemberAssignedToTaskEvent::class.java
            )
        } catch(e : Exception) {
        }
    }

    @Test
    fun testRemoveActualStatus(){
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )


        val projectTitle = "Test Project Status Addition"
        val creatorId = responseUser.body?.userId

        val createProjectResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, createProjectResponse.statusCode)
        val projectId = createProjectResponse.body?.projectId
        Assertions.assertNotNull(projectId)

        val statusTitle = "In Progress"
        val statusColor = "Blue"

        val addStatusResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle&color=$statusColor&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        val statusId = addStatusResponse.body?.statusId

        val taskTitle = "New Task"

        // Отправляем запрос на создание задачи
        val createTaskResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/tasks/$taskTitle?actorId=$creatorId",
            null,
            TaskCreatedEvent::class.java
        )

        val taskId = createTaskResponse.body?.taskId


        val changeTaskStatusResponse = restTemplate.patchForObject(
            "http://localhost:$port/projects/$projectId/tasks/$taskId/changeStatus/$statusId?actorId=$creatorId",
            null,
            TaskStatusChangedEvent::class.java
        )

        try {
            val removeTaskStatusResponse = restTemplate.delete(
                "http://localhost:$port/projects/$projectId/status/$statusId?actorId=$creatorId",
                null,
                StatusDeletedEvent::class.java
            )
        } catch (e : Exception) {

        }
    }
}