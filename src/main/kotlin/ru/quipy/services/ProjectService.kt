package ru.quipy.services

import org.springframework.stereotype.Service
import ru.quipy.entities.Project
import ru.quipy.entities.Status
import ru.quipy.entities.Task
import ru.quipy.repositories.ProjectRepository
import ru.quipy.repositories.StatusRepository
import ru.quipy.repositories.TaskRepository
import java.util.*

@Service
class ProjectService(
    val projectRepository: ProjectRepository,
    val taskRepository: TaskRepository,
    val statusRepository: StatusRepository) {

    fun getAllProjects() : List<Project>{
        return projectRepository.findAll()
    }

    fun getAllProjectTasks(projectId: UUID) : List<Task>{
        return taskRepository.findAllByProjectId(projectId)
    }

    fun getAllProjectStatuses(projectId: UUID) : List<Status>{
        return statusRepository.findAllByProjectId(projectId)
    }
}