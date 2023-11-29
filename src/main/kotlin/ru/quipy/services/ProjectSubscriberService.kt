package ru.quipy.services

import org.springframework.stereotype.Service
import ru.quipy.api.*
import ru.quipy.entities.Project
import ru.quipy.entities.Status
import ru.quipy.entities.Task
import ru.quipy.repositories.ProjectRepository
import ru.quipy.repositories.StatusRepository
import ru.quipy.repositories.TaskRepository
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct

@Service
class ProjectSubscriberService (
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val projectRepository: ProjectRepository,
    private val statusRepository: StatusRepository,
    private val taskRepository: TaskRepository
) {

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project-events-subscriber") {
            `when`(ProjectCreatedEvent::class) { event ->
                val entity = Project(event.projectId, event.title)
                projectRepository.save(entity)
            }
            `when`(ProjectTitleChangedEvent::class) { event ->
                val oldEntity = projectRepository.findByProjectId(event.projectId)
                val entity = Project(event.projectId, event.title)
                projectRepository.deleteProjectByProjectId(oldEntity.projectId)
                projectRepository.save(entity)
            }
            `when`(AddUserToProjectEvent::class) { event ->
                val oldEntity = projectRepository.findByProjectId(event.projectId)
                val members  = oldEntity.members;
                members.add(event.userId)
                val entity = Project(event.projectId, oldEntity.title, members)
                projectRepository.deleteProjectByProjectId(oldEntity.projectId)
                projectRepository.save(entity)
            }
            `when`(StatusCreatedEvent::class) { event ->
                val statusEntity = Status(event.statusId, event.projectId, event.statusName, event.color)
                statusRepository.save(statusEntity)
            }
            `when`(StatusDeletedEvent::class) { event ->
                statusRepository.deleteStatusByStatusId(event.statusId)
            }
            `when`(TaskCreatedEvent::class) {event ->
                val taskEntity = Task(event.taskId, event.projectId, event.title, null)
                taskRepository.save(taskEntity)
            }
            `when`(TaskStatusChangedEvent::class) {event ->
                val oldTaskEntity = taskRepository.findByTaskId(event.taskId)
                val taskEntity = Task(event.taskId, oldTaskEntity.projectId, oldTaskEntity.title, event.statusId)
                taskRepository.deleteTaskByTaskId(oldTaskEntity.taskId)
                taskRepository.save(taskEntity)
            }
            `when`(TaskTitleChangedEvent::class){event ->
                val oldTaskEntity = taskRepository.findByTaskId(event.taskId)
                val taskEntity = Task(event.taskId, oldTaskEntity.projectId, event.title, oldTaskEntity.statusId)
                taskRepository.deleteTaskByTaskId(oldTaskEntity.taskId)
                taskRepository.save(taskEntity)
            }
            `when`(MemberAssignedToTaskEvent::class){event ->
                val oldTaskEntity = taskRepository.findByTaskId(event.taskId)
                val executors = oldTaskEntity.executors
                executors.add(event.userId)
                val taskEntity = Task(event.taskId, oldTaskEntity.projectId, oldTaskEntity.title, oldTaskEntity.statusId, executors)
                taskRepository.deleteTaskByTaskId(oldTaskEntity.taskId)
                taskRepository.save(taskEntity)
            }
        }
    }
}