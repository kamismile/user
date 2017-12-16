package nl.spelberg.microservice.user.types

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class UserCreateCommand(@TargetAggregateIdentifier val id: UUID, val username: String)
data class UserDeleteCommand(@TargetAggregateIdentifier val id: UUID)
data class UserRegisterEmailCommand(@TargetAggregateIdentifier val id: UUID, val email: String)
data class UserValidateEmailCommand(@TargetAggregateIdentifier val id: UUID, val validationCode: String)

data class UserCreatedEvent(val id: UUID, val username: String)
data class UserDeletedEvent(val id: UUID)
data class UserRegisteredEmailEvent(val id: UUID, val email: Email, val validationCode: String)
data class UserValidatedEmailEvent(val id: UUID)

val emailRegex = Regex(
        "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")

data class Email(val address: String) : Validated<String>(address) {

    override fun validate(value: String) {
        if (!emailRegex.matches(value)) {
            throw IllegalArgumentException("Invalid email address: $value")
        }
    }
}


interface ValidationCodeFactory {
    fun generateValidationCode(): String
}

abstract class Validated<T>(value: T) {

    /**
     * Validates the value. Implementations should throw an exception when validation fails, and return normally when validation succeeds.
     * @param value the value to validate
     */
    abstract fun validate(value: T)

    init {
        validate(value)
    }

}