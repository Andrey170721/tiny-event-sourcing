package ru.quipy.controller

import org.springframework.web.bind.annotation.*
import ru.quipy.api.UserAggregate
import ru.quipy.api.UserChangedNameEvent
import ru.quipy.api.UserCreatedEvent
import ru.quipy.core.EventSourcingService
import ru.quipy.entities.User
import ru.quipy.logic.UserAggregateState
import ru.quipy.logic.changeName
import ru.quipy.logic.create
import ru.quipy.repositories.UserRepository
import ru.quipy.services.UserService
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    val userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>,
    val userService: UserService
) {

    @PostMapping("/1/{name}")
    fun addUser(@PathVariable name: String, @RequestParam nickname: String, @RequestParam password: String) : UserCreatedEvent {
        if (userService.getUserByNickname(nickname) != null){
            throw IllegalArgumentException("User with this nickname already exists")
        }
        return userEsService.create { it.create(UUID.randomUUID(), name, nickname, password) }
    }

    @GetMapping("/checkNickname/{nickname}")
    fun checkNickname(@PathVariable nickname: String): Boolean{
        if (userService.getUserByNickname(nickname) == null){
            return false
        }
        return true
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: UUID) : UserAggregateState? {
        return userEsService.getState(userId)
    }

    @GetMapping("/{nickname}")
    fun getUserByNickname(@PathVariable nickname: String) : User {
        val user = userService.getUserByNickname(nickname) ?: throw IllegalArgumentException("User does not exist")
        return user
    }

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