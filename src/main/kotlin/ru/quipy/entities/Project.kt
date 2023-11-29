package ru.quipy.entities

import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID
@Document("projects")
data class Project(
    var projectId: UUID,
    var title: String,
)