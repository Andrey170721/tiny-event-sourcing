package ru.quipy.entities

import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("tasks")
data class Task (
    var taskId: UUID,
    var projectId : UUID,
    var title : String,
    var statusId: UUID,
    var executors: MutableList<UUID> = mutableListOf()
)