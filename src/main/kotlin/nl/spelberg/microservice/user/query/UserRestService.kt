package nl.spelberg.microservice.user.query

import nl.spelberg.microservice.user.types.UserCreatedEvent
import nl.spelberg.microservice.user.types.UserDeletedEvent
import org.axonframework.eventhandling.EventHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Optional
import java.util.TreeMap

data class User(val id: String, val username: String)

@RestController
class UserRestService {

    private val users: MutableMap<String, User> = HashMap();
    private val usersByUsername: MutableMap<String, User> = TreeMap(String::compareTo);

/*
    @RequestMapping("/")
    fun index(): String {
        return "index.html"
    }
*/

    @RequestMapping("/users")
    fun users(): Collection<User> = usersByUsername.values

    @RequestMapping("/users/{id}")
    fun user(@PathVariable id: Optional<String>): User? {
        if (id.isPresent()) {
            val user = users[id.get()]
            if (user != null) {
                return user
            }
        };
        return null
    }

    @EventHandler
    fun on(event: UserCreatedEvent) {
        val userId = event.id.toString()
        users.put(userId, User(userId, event.username))
        usersByUsername.put(event.username, User(userId, event.username))
    }

    @EventHandler
    fun on(event: UserDeletedEvent) {
        val userId = event.id.toString()
        val user = users[userId]
        if (user != null) {
            usersByUsername.remove(user.username)
            users.remove(user.id)
        }
    }
}