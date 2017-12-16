package nl.spelberg.microservice.user.query

import nl.spelberg.microservice.user.types.UserCreatedEvent
import nl.spelberg.microservice.user.types.UserDeletedEvent
import org.axonframework.eventhandling.EventHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.TreeMap
import java.util.UUID

data class User(val id: UUID, val username: String)

@RestController
class UserRestService {

    private val usersByUsername: MutableMap<String, User> = TreeMap(String::compareTo);

    @RequestMapping("/")
    fun index(): String {
        return "User REST Service"
    }

    @RequestMapping("/users")
    fun users(): Collection<User> = usersByUsername.values

    @EventHandler
    fun on(event: UserCreatedEvent) {
        usersByUsername.put(event.username, User(event.id, event.username))
    }

    @EventHandler
    fun on(event: UserDeletedEvent) {
//        usersByUsername.remove(event.username, User(event.id, event.username))
    }
}