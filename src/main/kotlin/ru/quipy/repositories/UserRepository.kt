package ru.quipy.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import ru.quipy.entities.Project
import ru.quipy.entities.User
import java.util.*

interface UserRepository : MongoRepository<User, UUID> {
    fun findByNickname(nickname: String) : User

    fun findByUserId(userId : UUID): User
}