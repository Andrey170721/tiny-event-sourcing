package ru.quipy.entities

import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("statuses")
class Status (
    val statusId: UUID,
    val projectId: UUID,
    val name: String?,
    val color: String?
)