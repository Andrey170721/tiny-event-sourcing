package ru.quipy.controller

import org.springframework.web.bind.annotation.*
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserChangedNameEvent
import ru.quipy.api.UserCreatedEvent
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.UserAggregateState
import ru.quipy.logic.changeName
import ru.quipy.logic.create
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    val userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>,
) {

    @PostMapping("/1/{name}")
    fun addUser(@PathVariable name: String, @RequestParam nickname: String, @RequestParam password: String) : UserCreatedEvent {

        return userEsService.create { it.create(UUID.randomUUID(), name, nickname, password) }
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: UUID) : UserAggregateState? {
        return userEsService.getState(userId)
    }

//    @GetMapping("/{nickname}")
//    fun checkIfNicknameExists(@PathVariable nickname: String) : Boolean {
//        return userEsService.getState(nickname) != null
//    }
//
//    @GetMapping("/findByNicknameSubstr/{nicknameSubstr}")
//    fun findByNicknameSubstr(@PathVariable nicknameSubstr: String) : List<UserViewDomain.User> {
//        return userViewService.findByNameSubs(nicknameSubstr)
//    }

    @PostMapping("/{userId}")
    fun changeUserName(@PathVariable userId: UUID, @RequestParam newName: String) : UserChangedNameEvent {
        return userEsService.update(userId) {
            it.changeName(newName)
        }
    }
}