package ru.quipy.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.*
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
) {

    @PostMapping("/{projectTitle}")
    fun createProject(@PathVariable projectTitle: String, @RequestParam creatorId: String) : ProjectCreatedEvent {
        return projectEsService.create { it.create(UUID.randomUUID(), projectTitle, creatorId) }
    }

    @GetMapping("/{projectId}")
    fun getAccount(@PathVariable projectId: UUID) : ProjectAggregateState? {
        return projectEsService.getState(projectId)
    }

    @GetMapping("/{projectId}/tasks/{taskId}")
    fun getTask(@PathVariable projectId: UUID, @PathVariable taskId: UUID) : TaskEntity? {
        val state = projectEsService.getState(projectId);
        return state?.getTask(taskId)
    }

    @PostMapping("/{projectId}/tasks/{taskName}")
    fun createTask(@PathVariable projectId: UUID, @PathVariable taskName: String) : TaskCreatedEvent {
        return projectEsService.update(projectId) {
            it.addTask(taskName)
        }
    }

    @PostMapping("/1/{projectId}")
    fun createTag(@PathVariable projectId: UUID, @RequestParam tagName: String): TagCreatedEvent{
        return projectEsService.update(projectId){
            it.createTag(tagName)
        }
    }

    @PostMapping("/assignTag/{projectId}/{taskId}/{tagId}")
    fun assignTag(@PathVariable projectId: UUID, @PathVariable taskId: UUID, @PathVariable tagId : UUID): TagAssignedToTaskEvent{
        return projectEsService.update(projectId){
            it.assignTagToTask(tagId, taskId)
        }
    }
}