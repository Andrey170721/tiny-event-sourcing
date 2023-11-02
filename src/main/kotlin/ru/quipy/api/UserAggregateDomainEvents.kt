package ru.quipy.api

import ru.quipy.core.annotations.DomainEvent
import java.util.*
import ru.quipy.domain.Event

const val USER_CREATED_EVENT = "USER_CREATED_EVENT"
const val USER_CHANGED_NAME_EVENT = "USER_CHANGED_NAME_EVENT"

@DomainEvent(name = USER_CREATED_EVENT)
class UserCreatedEvent(
    val userId: UUID,
    val firstname: String,
    val nickname: String,
    val password: String,
    createdAt: Long = System.currentTimeMillis(),
) : Event<UserAggregate>(
    name = USER_CREATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = USER_CHANGED_NAME_EVENT)
class UserChangedNameEvent(
    val userId: UUID,
    val newName: String,
    createdAt: Long = System.currentTimeMillis(),
) : Event<UserAggregate>(
    name = USER_CREATED_EVENT,
    createdAt = createdAt,
)