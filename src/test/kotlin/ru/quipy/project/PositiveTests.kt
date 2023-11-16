package ru.quipy.project

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import ru.quipy.api.*
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.logic.UserAggregateState
import java.net.URI
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PositiveTests {

    @LocalServerPort
    private val port: Int = 8080

    @Autowired
    private lateinit var restTemplate: TestRestTemplate


    @Test
    fun testCreateProject() {
        val projectTitle = "Test Project"

        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

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
        // Создаем юзера и проект
        val projectTitle = "Test Project"

        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем успешное создание проекта
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val createdProjectId = response.body?.projectId
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
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
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
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем успешное создание проекта
        Assertions.assertEquals(HttpStatus.OK, createProjectResponse.statusCode)
        val createdProjectId = createProjectResponse.body?.projectId
        Assertions.assertNotNull(createdProjectId)

        // Создаем пользователя 2
        val userName1 = UUID.randomUUID().toString()
        val nickname1 = UUID.randomUUID().toString()
        val password1 = UUID.randomUUID().toString()

        val createUserResponse1 = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName1?nickname=$nickname1&password=$password1",
            null,
            UserCreatedEvent::class.java
        )

        // Проверяем успешное создание пользователя
        Assertions.assertEquals(HttpStatus.OK, createUserResponse1.statusCode)
        val createdUserId1 = createUserResponse1.body?.userId
        Assertions.assertNotNull(createdUserId1)

        // Добавляем пользователя к проекту
        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$createdProjectId/users/$createdUserId1?actorId=$creatorId",
            null,
            AddUserToProjectEvent::class.java
        )

        // Проверяем успешное добавление пользователя к проекту
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val addUserToProjectEvent = response.body
        Assertions.assertNotNull(addUserToProjectEvent)
        Assertions.assertEquals(createdProjectId, addUserToProjectEvent?.projectId)
        Assertions.assertEquals(createdUserId1, addUserToProjectEvent?.userId)
    }

    @Test
    fun testChangeProjectTitle() {

        // Создаём юзера

        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        // Создаем проект
        val originalTitle = "Original Project Title"
        val newTitle = "New Project Title"
        val creatorId = responseUser.body?.userId

        val createProjectResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$originalTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем успешное создание проекта
        Assertions.assertEquals(HttpStatus.OK, createProjectResponse.statusCode)
        val projectId = createProjectResponse.body?.projectId
        Assertions.assertNotNull(projectId)

        // Изменяем название проекта
        val changeTitleResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/${projectId}/changeTitle/$newTitle?actorId=$creatorId",
            null,
            ProjectTitleChangedEvent::class.java
        )

        // Проверяем успешное изменение названия проекта
        Assertions.assertEquals(HttpStatus.OK, changeTitleResponse.statusCode)
        val projectTitleChangedEvent = changeTitleResponse.body
        Assertions.assertNotNull(projectTitleChangedEvent)
        Assertions.assertEquals(projectId, projectTitleChangedEvent?.projectId)
        Assertions.assertEquals(newTitle, projectTitleChangedEvent?.title)
    }

    @Test
    fun testAddStatusToProject() {
        // First, create a project as in the previous tests

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

        // Now, add a status to the project
        val statusTitle = "In Progress"
        val statusColor = "Blue"

        val addStatusResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle&color=$statusColor&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        // Check that the status was added successfully
        Assertions.assertEquals(HttpStatus.OK, addStatusResponse.statusCode)
        val statusCreatedEvent = addStatusResponse.body
        Assertions.assertNotNull(statusCreatedEvent)
        Assertions.assertNotNull(statusCreatedEvent?.statusId)
        Assertions.assertEquals(statusTitle, statusCreatedEvent?.statusName)
    }

    @Test
    fun testCreateTask() {
        // Предполагаем, что проект уже создан
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

        val projectId = createProjectResponse.body?.projectId

        // Данные для создания задачи
        val taskTitle = "New Task"

        // Отправляем запрос на создание задачи
        val createTaskResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/tasks/$taskTitle?actorId=$creatorId",
            null,
            TaskCreatedEvent::class.java
        )

        // Проверяем успешное создание задачи
        Assertions.assertEquals(HttpStatus.OK, createTaskResponse.statusCode)
        val taskCreatedEvent = createTaskResponse.body
        Assertions.assertNotNull(taskCreatedEvent)
        Assertions.assertNotNull(taskCreatedEvent?.taskId)
        Assertions.assertEquals(taskTitle, taskCreatedEvent?.title)
    }

    @Test
    fun testAssignStatusToTask() {
        // Assume we have already created a project and added a status to it
        // Also, assume we have created a task in the project as previous tests
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

        // Now, add a status to the project
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

        // Check that the task status was changed successfully
        Assertions.assertNotNull(changeTaskStatusResponse)
        Assertions.assertEquals(taskId, changeTaskStatusResponse?.taskId)
        Assertions.assertEquals(statusId, changeTaskStatusResponse?.statusId)
    }
}
