package nl.spelberg.microservice.user.domain

import nl.spelberg.microservice.user.types.Email
import nl.spelberg.microservice.user.types.UserCreateCommand
import nl.spelberg.microservice.user.types.UserCreatedEvent
import nl.spelberg.microservice.user.types.UserDeleteCommand
import nl.spelberg.microservice.user.types.UserDeletedEvent
import nl.spelberg.microservice.user.types.UserRegisterEmailCommand
import nl.spelberg.microservice.user.types.UserRegisteredEmailEvent
import nl.spelberg.microservice.user.types.UserValidateEmailCommand
import nl.spelberg.microservice.user.types.UserValidatedEmailEvent
import nl.spelberg.microservice.user.types.ValidationCodeFactory
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.commandhandling.model.AggregateLifecycle.markDeleted
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate


@Aggregate
class UserAggregate() {

    @AggregateIdentifier
    private lateinit var id: String

    private var emailValidationCode: String? = null

    @CommandHandler
    constructor(command: UserCreateCommand) : this() {
        apply(UserCreatedEvent(command.id, command.username));
    }

    @CommandHandler
    fun handle(command: UserDeleteCommand) {
        apply(UserDeletedEvent(command.id));
    }

    @CommandHandler
    fun handle(command: UserRegisterEmailCommand, validationCodeFactory: ValidationCodeFactory) {
        apply(UserRegisteredEmailEvent(command.id, Email(command.email), validationCodeFactory.generateValidationCode()));
    }

    @CommandHandler
    fun handle(command: UserValidateEmailCommand) {
        if (emailValidationCode == null || emailValidationCode != command.validationCode) {
            throw IllegalArgumentException("Invalid validationCode")
        }
        apply(UserValidatedEmailEvent(id))

    }

    @EventSourcingHandler
    fun on(event: UserCreatedEvent) {
        this.id = event.id
    }

    @EventSourcingHandler
    fun on(event: UserDeletedEvent) {
        markDeleted()
    }

    @EventSourcingHandler
    fun on(event: UserRegisteredEmailEvent) {
        emailValidationCode = event.validationCode
    }
}