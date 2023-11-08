package ru.quipy.controller

import org.springframework.web.bind.annotation.*
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.*
import ru.quipy.projections.AnnotationBasedProjectEventsSubscriber
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
) {

    @PostMapping("/{projectTitle}")
    fun createProject(@PathVariable projectTitle: String, @RequestParam creatorId: UUID) : ProjectCreatedEvent {
        return projectEsService.create { it.create(UUID.randomUUID(), projectTitle, creatorId) }
    }

    @GetMapping("/{projectId}")
    fun getProject(@PathVariable projectId: UUID) : ProjectAggregateState? {
        return projectEsService.getState(projectId)
    }

    @PostMapping("/{projectId}/users/{userId}")
    fun addUserToProject(@PathVariable projectId: UUID, @PathVariable userId: UUID, @RequestParam actorId: UUID) : AddUserToProjectEvent {
        return projectEsService.update(projectId) { it.addUser(projectId, userId, actorId) }
    }

    @PatchMapping("/{projectId}/changeTitle/{newTitle}")
    fun changeProjectTitle(@PathVariable projectId: UUID, @PathVariable newTitle: String, @RequestParam actorId: UUID): ProjectTitleChangedEvent {
        return projectEsService.update(projectId){
            it.changeTitle(projectId, newTitle, actorId)
        }
    }

    @PostMapping("/{projectId}/status")
    fun createStatus(@PathVariable projectId: UUID, @RequestBody title: String, @RequestBody color: String) : StatusCreatedEvent {
        return projectEsService.update(projectId) { it.addStatus (title, color) }
    }

    @DeleteMapping("/{projectId}/status/{statusId}")
    fun removeStatus(@PathVariable projectId: UUID, @PathVariable statusId: UUID): StatusDeletedEvent {
        return projectEsService.update(projectId){ it.removeStatus(statusId, projectId) }
    }


    @PostMapping("/{projectId}/tasks/{taskName}")
    fun createTask(@PathVariable projectId: UUID, @PathVariable taskName: String) : TaskCreatedEvent {
        return projectEsService.update(projectId) {
            it.addTask(taskName)
        }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/changeTitle/{newTitle}")
    fun changeTaskTitle(@PathVariable projectId: UUID, @PathVariable taskId: UUID, @PathVariable newTitle: String): TaskTitleChangedEvent {
        return projectEsService.update(projectId){ it.changeTaskTitle(taskId, newTitle) }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/changeStatus/{newStatusId}")
    fun changeTaskStatus(@PathVariable projectId: UUID, @PathVariable taskId: UUID, @PathVariable newStatusId: UUID): TaskStatusChangedEvent {
        return projectEsService.update(projectId){ it.changeTaskStatus(taskId, newStatusId) }
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/assign/{userId}")
    fun memberAssignToTask(@PathVariable projectId: UUID, @PathVariable taskId: UUID, @PathVariable userId: UUID) : MemberAssignedToTaskEvent {
        return projectEsService.update(projectId){ it.memberAssignedToTask(userId, taskId) }
    }

    @GetMapping("/{projectId}/getTasks")
    fun getTasks(@PathVariable projectId: UUID): MutableMap<UUID, TaskEntity>? {
        var projectAggregateState = projectEsService.getState(projectId)
        return projectAggregateState?.tasks
    }
}