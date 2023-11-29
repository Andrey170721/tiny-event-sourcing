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
class ConsistencyBreakingTest2 {

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
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle2&color=$statusColor2&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        val statusTitle3 = "Not started"
        val statusColor3 = "Green"
        var addStatusResponse3 = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle3&color=$statusColor3&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        val statusTitle4 = "Overdue"
        val statusColor4 = "Yellow"
        var addStatusResponse4 = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/status?title=$statusTitle4&color=$statusColor4&actorId=$creatorId",
            null,
            StatusCreatedEvent::class.java
        )

        val statusId = addStatusResponse.body?.statusId
        val statusId2 = addStatusResponse2.body?.statusId
        val statusId3 = addStatusResponse3.body?.statusId
        val statusId4 = addStatusResponse4.body?.statusId

        val taskTitle = "New Task"

        // Отправляем запрос на создание задачи
        val createTaskResponse = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectId/tasks/$taskTitle?actorId=$creatorId",
            null,
            TaskCreatedEvent::class.java
        )

        val taskId = createTaskResponse.body?.taskId

        val statusCreateResponse = restTemplate.patchForObject(
            "http://localhost:$port/projects/$projectId/tasks/$taskId/changeStatus/$statusId?actorId=$creatorId",
            null,
            TaskStatusChangedEvent::class.java
        )

        val getTasksResponse = restTemplate.getForEntity(
            "http://localhost:$port/projects/$projectId/getTasks",
            ProjectAggregateState::class.java
        )

        println(getTasksResponse.body?.tasks?.get(taskId)?.status)

            // Добавляем и удаляем статус одновременно
            val job = GlobalScope.launch {
                restTemplate.patchForObject(
                    "http://localhost:$port/projects/$projectId/tasks/$taskId/changeStatus/$statusId2?actorId=$creatorId",
                    null,
                    TaskStatusChangedEvent::class.java
                )
            }

            val job2 = GlobalScope.launch {
                restTemplate.delete(
                    "http://localhost:$port/projects/$projectId/status/$statusId?actorId=$creatorId"
                )
            }

        val job3 = GlobalScope.launch {
            restTemplate.patchForObject(
                "http://localhost:$port/projects/$projectId/tasks/$taskId/changeStatus/$statusId3?actorId=$creatorId",
                null,
                TaskStatusChangedEvent::class.java
            )
        }

        val job4 = GlobalScope.launch {
            restTemplate.patchForObject(
                "http://localhost:$port/projects/$projectId/tasks/$taskId/changeStatus/$statusId4?actorId=$creatorId",
                null,
                TaskStatusChangedEvent::class.java
            )
        }

        runBlocking { job.join()
        job2.join()
        job3.join()
        job4.join()}

        val getTasksResponse2 = restTemplate.getForEntity(
            "http://localhost:$port/projects/$projectId/getTasks",
            ProjectAggregateState::class.java
        )

        println(getTasksResponse2.body?.tasks?.get(taskId)?.status.toString())
    }
}