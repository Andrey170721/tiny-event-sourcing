package ru.quipy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import ru.quipy.api.*
import ru.quipy.logic.ProjectAggregateState
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConsistencyBreakingTest {

    @LocalServerPort
    private val port: Int = 8080

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun testConcurrentAddAndRemoveStatusFromProject() {
        // Создаем проект, как в предыдущих тестах
        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val projectTitle = UUID.randomUUID().toString()
        val createProjectResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        val projectId = createProjectResponse.body?.projectId


        val statusTitle = "In Progress"
        val statusColor = "Blue"
        var addStatusResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle&color=$statusColor&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        val statusTitle2 = "Finished"
        val statusColor2 = "Red"
        var addStatusResponse2 = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle&color=$statusColor&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        val statusId = addStatusResponse.body?.statusId
        val statusId2 = addStatusResponse.body?.statusId

        val taskTitle = "New Task"

        // Отправляем запрос на создание задачи
        val createTaskResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/tasks/$taskTitle?actorId=$creatorId",
            null,
            TaskCreatedEvent::class.java
        )

        val taskId = createTaskResponse.body?.taskId


            // Добавляем и удаляем статус одновременно
            runBlocking {  launch {
                restTemplate.patchForObject(
                    "http://localhost:$port/projects/$projectId/tasks/$taskId/changeStatus/$statusId2?actorId=$creatorId",
                    null,
                    TaskStatusChangedEvent::class.java
                )
            }
            launch {
                restTemplate.delete(
                    "http://localhost:$port/projects/$projectId/status/$statusId?actorId=$creatorId"
                )
            }
        }

        val getTasksResponse = restTemplate.getForEntity(
            "http://localhost:$port/projects/$projectId/getTasks",
            ProjectAggregateState::class.java
        )

        println(getTasksResponse.body?.tasks?.get(taskId)?.status)
    }
}