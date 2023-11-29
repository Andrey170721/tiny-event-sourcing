package ru.quipy.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.entities.Task
import java.util.*

interface TaskRepository : MongoRepository<Task, UUID> {
    fun findByTaskId(taskId: UUID): Task

    fun deleteTaskByTaskId(taskId: UUID)

    fun findAllByProjectId(projectId: UUID): List<Task>
}
