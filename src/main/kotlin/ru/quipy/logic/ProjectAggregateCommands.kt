package ru.quipy.logic

import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import java.util.*


// Commands : takes something -> returns event
// Here the commands are represented by extension functions, but also can be the class member functions

fun ProjectAggregateState.create(id: UUID, title: String, creatorId: UUID): ProjectCreatedEvent {
    val userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>
    return ProjectCreatedEvent(projectId = id, title = title, creatorId = creatorId)
}

fun ProjectAggregateState.addUser(projectId: UUID, userId: UUID, actorId: UUID): AddUserToProjectEvent {
    var userAlreadyExist = false;
    this.projectMemberIds.forEach { element ->
        if (element == userId){
            userAlreadyExist = true;
        }
    }

    if (userAlreadyExist){
        throw IllegalArgumentException("User already exist in this project: $userId")
    }
    checkPermissions(actorId)
    return AddUserToProjectEvent(projectId = projectId, userId = userId);
}

fun ProjectAggregateState.changeTitle(projectId: UUID, title: String, actorId: UUID): ProjectTitleChangedEvent {
    if (Objects.equals(actorId, creatorId)) {
        return ProjectTitleChangedEvent(projectId = projectId, title = title)
    } else {
        throw IllegalArgumentException("User does not have permissions $actorId")
    }
}

fun ProjectAggregateState.addStatus(name: String, color: String, actorId: UUID): StatusCreatedEvent {
    checkPermissions(actorId)
    return StatusCreatedEvent(projectId = this.getId(), statusId = UUID.randomUUID(), statusName = name, color = color)
}

fun ProjectAggregateState.removeStatus(statusId: UUID, projectId: UUID, actorId: UUID): StatusDeletedEvent {
    checkPermissions(actorId)
    if (!projectStatus.containsKey(statusId)){
        throw IllegalArgumentException("Status doesn't exists: $statusId")
    }

    var statusIsUsed = false;
    this.tasks.forEach { element ->
        if (element.value.status == statusId){
            statusIsUsed = true;
        }
    }

    if (statusIsUsed){
        throw IllegalArgumentException("Status is used: $statusId")
    }

    return StatusDeletedEvent(statusId, projectId)
}

fun ProjectAggregateState.addTask(name: String, actorId: UUID): TaskCreatedEvent {
    checkPermissions(actorId)
    return TaskCreatedEvent(projectId = this.getId(), taskId = UUID.randomUUID(), title = name)
}

fun ProjectAggregateState.changeTaskTitle(taskId: UUID, title: String, actorId: UUID): TaskTitleChangedEvent{
    checkPermissions(actorId)
    return  TaskTitleChangedEvent(taskId = taskId, title = title)
}

fun ProjectAggregateState.changeTaskStatus(taskId: UUID, statusId: UUID, actorId: UUID): TaskStatusChangedEvent{
    checkPermissions(actorId)
    if (!projectStatus.containsKey(statusId)){
        throw IllegalArgumentException("Status doesn't exists: $statusId")
    }

    return TaskStatusChangedEvent(taskId = taskId, statusId = statusId)
}

fun ProjectAggregateState.memberAssignedToTask(userId: UUID, taskId: UUID, actorId: UUID): MemberAssignedToTaskEvent{
    checkPermissions(actorId)
    checkPermissions(userId)
    var userNotExist = true;
    var taskNotExist = true;

    this.projectMemberIds.forEach { element ->
        if (element == userId){
            userNotExist = false;
        }
    }

    this.tasks.forEach { element ->
        if (element.value.id == taskId){
            taskNotExist = true;
        }
    }

    if (userNotExist){
        throw IllegalArgumentException("User not exist: $userId")
    }

    if (taskNotExist){
        throw IllegalArgumentException("Task not exist: $taskId")
    }

    return MemberAssignedToTaskEvent(taskId = taskId, userId = userId)
}

fun ProjectAggregateState.checkPermissions(userId: UUID){
    var accessError = true;
    this.projectMemberIds.forEach{ element ->
        if(element == userId){
            accessError = false;
        }
    }
    if (!Objects.equals(userId, this.creatorId) || accessError){
        throw IllegalArgumentException("User does not have permissions: $userId")
    }
}