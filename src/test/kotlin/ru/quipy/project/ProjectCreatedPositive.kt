package ru.quipy.project

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import ru.quipy.api.*
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.logic.UserAggregateState
import java.net.URI
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

    @Test
    fun testCreateUser() {
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val response = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        // Проверяем, что запрос вернул статус 200 OK
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)

        // Проверяем, что ответ содержит данные созданного пользователя
        val userCreatedEvent = response.body
        Assertions.assertNotNull(userCreatedEvent)
        Assertions.assertNotNull(userCreatedEvent?.userId)
        Assertions.assertEquals(userName, userCreatedEvent?.firstname)
        Assertions.assertEquals(nickname, userCreatedEvent?.nickname)
        Assertions.assertEquals(password, userCreatedEvent?.password)
    }

    @Test
    fun testGetUser() {
        // Создаем пользователя
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val createUserResponse = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        // Проверяем успешное создание пользователя
        Assertions.assertEquals(HttpStatus.OK, createUserResponse.statusCode)
        val createdUserId = createUserResponse.body?.userId
        Assertions.assertNotNull(createdUserId)

        // Получаем пользователя
        val getUserResponse = restTemplate.getForEntity(
            "http://localhost:$port/users/$createdUserId",
            UserAggregateState::class.java
        )

        // Проверяем успешный ответ от сервера
        Assertions.assertEquals(HttpStatus.OK, getUserResponse.statusCode)

        // Проверяем данные полученного пользователя
        val userAggregateState = getUserResponse.body
        Assertions.assertNotNull(userAggregateState)
        Assertions.assertEquals(userName, userAggregateState?.name)
        Assertions.assertEquals(nickname, userAggregateState?.nickname)
        Assertions.assertEquals(password, userAggregateState?.password)
    }

    @Test
    fun testGetProject() {
        // Создаем проект
        val projectTitle = "Test Project"
        val creatorId = UUID.randomUUID()

        val createProjectResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем успешное создание проекта
        Assertions.assertEquals(HttpStatus.OK, createProjectResponse.statusCode)
        val createdProjectId = createProjectResponse.body?.projectId
        Assertions.assertNotNull(createdProjectId)

        // Получаем проект по идентификатору
        val getProjectResponse = restTemplate.getForEntity(
            "http://localhost:$port/projects/$createdProjectId",
            ProjectAggregateState::class.java
        )

        // Проверяем успешный ответ от сервера
        Assertions.assertEquals(HttpStatus.OK, getProjectResponse.statusCode)

        // Проверяем данные полученного проекта
        val projectAggregateState = getProjectResponse.body
        Assertions.assertNotNull(projectAggregateState)
        Assertions.assertEquals(projectTitle, projectAggregateState?.projectTitle)
        // Убедитесь, что здесь есть все поля, которые вы хотите проверить, например, создатель проекта.
    }

    @Test
    fun testAddUserToProject() {
        // Создаем пользователя
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val createUserResponse = restTemplate.postForEntity(
            "http://localhost:$port/users?name=$userName&nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        // Проверяем успешное создание пользователя
        Assertions.assertEquals(HttpStatus.OK, createUserResponse.statusCode)
        val createdUserId = createUserResponse.body?.userId
        Assertions.assertNotNull(createdUserId)

        // Создаем проект
        val projectTitle = UUID.randomUUID().toString()
        val creatorId = createdUserId!!  // Предположим, что пользователь является создателем проекта

        val createProjectResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects?title=$projectTitle&creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем успешное создание проекта
        Assertions.assertEquals(HttpStatus.OK, createProjectResponse.statusCode)
        val createdProjectId = createProjectResponse.body?.projectId
        Assertions.assertNotNull(createdProjectId)

        // Добавляем пользователя к проекту
        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$createdProjectId/users/$createdUserId?actorId=$creatorId",
            null,
            AddUserToProjectEvent::class.java
        )

        // Проверяем успешное добавление пользователя к проекту
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val addUserToProjectEvent = response.body
        Assertions.assertNotNull(addUserToProjectEvent)
        Assertions.assertEquals(createdProjectId, addUserToProjectEvent?.projectId)
        Assertions.assertEquals(createdUserId, addUserToProjectEvent?.userId)
    }
}
