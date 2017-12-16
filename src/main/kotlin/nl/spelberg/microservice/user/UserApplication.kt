package nl.spelberg.microservice.user

import nl.spelberg.microservice.user.types.RandomUUIDValidationCodeFactory
import nl.spelberg.microservice.user.types.ValidationCodeFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class UserApplication {
    @Bean
    fun validationCodeFactory(): ValidationCodeFactory {
        return RandomUUIDValidationCodeFactory()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(UserApplication::class.java, *args)
}