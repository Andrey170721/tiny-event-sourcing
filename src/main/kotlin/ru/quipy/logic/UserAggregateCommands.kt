package ru.quipy.logic

import ru.quipy.api.UserChangedNameEvent
import ru.quipy.api.UserCreatedEvent
import ru.quipy.repositories.UserRepository
import ru.quipy.services.UserSubscriberService
import java.util.*

fun UserAggregateState.create(id: UUID, name: String, nickname: String, password: String): UserCreatedEvent {
    val userService : UserSubscriberService

    return UserCreatedEvent(
        userId = id,
        firstname = name,
        nickname = nickname,
        password = password
    )
}

fun UserAggregateState.changeName(newName: String): UserChangedNameEvent {
    return UserChangedNameEvent(
        userId = getId(),
        newName
    )
}