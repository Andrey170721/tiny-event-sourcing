package ru.quipy.services

import org.springframework.stereotype.Service
import ru.quipy.api.AddUserToProjectEvent
import ru.quipy.api.ProjectAggregate
import ru.quipy.api.ProjectCreatedEvent
import ru.quipy.api.ProjectTitleChangedEvent
import ru.quipy.entities.Project
import ru.quipy.repositories.ProjectRepository
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct

@Service
class ProjectSubscriberService (
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val projectRepository: ProjectRepository
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
//            `when`(AddUserToProjectEvent::class) { event ->
//                val oldEntity = projectRepository.findById(event.projectId).get()
//                entity.participants.add(event.addedUserId)
//                projectRepository.save(entity)
//            }
//            `when`(ParticipantRemovedEvent::class) { event ->
//                val entity = projectRepository.findById(event.projectId).get()
//                entity.participants.remove(event.removedUserId)
//                projectRepository.save(entity)
//            }
//            `when`(TaskStatusCreatedEvent::class) { event ->
//                val entity = projectRepository.findById(event.projectId).get()
//                entity.taskStatuses.add(event.statusId)
//                projectRepository.save(entity)
//            }
//            `when`(TaskStatusRemovedEvent::class) { event ->
//                val entity = projectRepository.findById(event.projectId).get()
//                entity.taskStatuses.remove(event.statusId)
//                projectRepository.save(entity)
//            }
        }
    }
}