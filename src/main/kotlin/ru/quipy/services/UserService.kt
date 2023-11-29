package ru.quipy.services

import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.stereotype.Service
import ru.quipy.entities.User
import ru.quipy.repositories.UserRepository


@Service
class UserService(private val userRepository: UserRepository) {

    fun getUserByNickname(nickname: String): User? {
       return try {
           userRepository.findByNickname(nickname)
        }catch (e : IncorrectResultSizeDataAccessException){
            null
        }

    }
}
