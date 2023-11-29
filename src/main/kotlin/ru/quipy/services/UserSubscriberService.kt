package ru.quipy.services

import org.springframework.stereotype.Service
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserChangedNameEvent
import ru.quipy.api.UserCreatedEvent
import ru.quipy.entities.User
import ru.quipy.repositories.UserRepository
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct
@Service
class UserSubscriberService (
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val userRepository: UserRepository
){
    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "user-events-subscriber") {
            `when`(UserCreatedEvent::class) { event ->
                val entity = User(event.userId, event.firstname, event.nickname)
                userRepository.save(entity)

            }
            `when`(UserChangedNameEvent::class) { event ->
                val oldEntity = userRepository.findByUserId(event.userId)
                val entity = User(event.userId, event.newName, oldEntity.nickname)
                userRepository.delete(oldEntity)
                userRepository.save(entity)
            }
        }
    }
}