package ru.quipy.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.entities.Project
import java.util.UUID

interface ProjectRepository : MongoRepository<Project, UUID> {
    fun findByProjectId(projectId : UUID): Project
    fun deleteProjectByProjectId(projectId: UUID)
}