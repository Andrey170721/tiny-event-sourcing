package ru.quipy.logic

import com.mongodb.client.MongoClients
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import ru.quipy.api.UserChangedNameEvent
import ru.quipy.api.UserCreatedEvent
import java.util.*

fun UserAggregateState.create(id: UUID, name: String, nickname: String, password: String): UserCreatedEvent {

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