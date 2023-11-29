package ru.quipy.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.entities.Status
import java.util.UUID

interface StatusRepository : MongoRepository <Status, UUID> {
    fun deleteStatusByStatusId(statusId : UUID)

    fun findAllByProjectId(projectId : UUID) : List<Status>
}