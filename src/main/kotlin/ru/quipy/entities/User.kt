package ru.quipy.entities;

import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("users")
data class User (
        var userId: UUID,
        var name : String,
        var nickname : String,
        )
