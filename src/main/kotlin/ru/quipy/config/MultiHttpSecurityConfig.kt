package ru.quipy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import ru.quipy.api.UserAggregate
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.UserAggregateState
import java.util.*

@Configuration
@EnableWebSecurity
class MultiHttpSecurityConfig {
    private lateinit var userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>
    @Bean
    public fun userDetailsService(): UserDetailsService {
        val userState = userEsService
        val users: User.UserBuilder = User.withDefaultPasswordEncoder()
        val manager = InMemoryUserDetailsManager()
        manager.createUser(users.username("user").password("password").roles("USER").build())
        manager.createUser(users.username("admin").password("password").roles("USER","ADMIN").build())
        return manager
    }

    @Bean
    open fun formLoginFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize(anyRequest, authenticated)
            }
            formLogin { }
        }
        return http.build()
    }
}