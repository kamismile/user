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
import org.axonframework.eventsourcing.AggregateDeletedException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.UUID

object UserAggregateTest : Spek({
    val id = UUID.randomUUID()
    val username = "myNameIs"

    describe("UserAggregate lifecycle") {
        on("create") {
            it("should emit UserCreatedEvent($id, $username)") {
                AggregateTestFixture(UserAggregate::class.java)
                        .given()
                        .`when`(UserCreateCommand(id, username))
                        .expectEvents(UserCreatedEvent(id, username))
            }
        }
        on("delete") {
            it("should emit UserDeletedEvent($id)") {
                AggregateTestFixture(UserAggregate::class.java)
                        .given(UserCreatedEvent(id, username))
                        .`when`(UserDeleteCommand(id))
                        .expectEvents(UserDeletedEvent(id))
            }
        }
        on("delete twice") {
            it("should throw AggregateDeletedException") {
                AggregateTestFixture(UserAggregate::class.java)
                        .given(UserCreatedEvent(id, username), UserDeletedEvent(id))
                        .`when`(UserDeleteCommand(id))
                        .expectException(AggregateDeletedException::class.java)
            }
        }
    }

    describe("Email address validation") {
        val address = "user@email.domain"
        val validationCode = "42"
        val validationCodeFactory = mock(ValidationCodeFactory::class.java)
        Mockito.`when`(validationCodeFactory.generateValidationCode()).thenReturn(validationCode)

        on("register") {
            it("should emit event with validationCode") {
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username))
                        .`when`(UserRegisterEmailCommand(id, address))
                        .expectEvents(UserRegisteredEmailEvent(id, Email(address), validationCode))
            }
        }

        on("register with invalid email") {
            it("should throw") {
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username))
                        .`when`(UserRegisterEmailCommand(id, "user-email.domain"))
                        .expectException(IllegalArgumentException::class.java)
            }
        }

        on("validate") {
            it("should emit event") {
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username), UserRegisteredEmailEvent(id, Email(address), validationCode))
                        .`when`(UserValidateEmailCommand(id, validationCode))
                        .expectEvents(UserValidatedEmailEvent(id))
            }
        }

        on("validate with wrong validationCode") {
            it("should throw") {
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username), UserRegisteredEmailEvent(id, Email(address), validationCode))
                        .`when`(UserValidateEmailCommand(id, "43"))
                        .expectException(IllegalArgumentException::class.java)
            }
        }

        on("validate with null validationCode") {
            it("should throw") {
                val userValidateEmailCommand = UserValidateEmailCommand(id, "")
                val field = userValidateEmailCommand::class.java.getDeclaredField("validationCode")
                field.isAccessible = true
                field.set(userValidateEmailCommand, null)
                println("With null: $userValidateEmailCommand")
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username))
                        .`when`(userValidateEmailCommand)
                        .expectException(IllegalArgumentException::class.java)
            }
        }

        on("validate before register") {
            it("should throw") {
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username))
                        .`when`(UserValidateEmailCommand(id, validationCode))
                        .expectException(IllegalArgumentException::class.java)
            }
        }

        on("register other emailaddress before validation") {
            it("should emit event with new validationCode") {
                Mockito.`when`(validationCodeFactory.generateValidationCode()).thenReturn("456")
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username), UserRegisteredEmailEvent(id, Email(address), validationCode))
                        .`when`(UserRegisterEmailCommand(id, "other@email.domain"))
                        .expectEvents(UserRegisteredEmailEvent(id, Email("other@email.domain"), "456"))
            }
        }

        on("register other emailaddress after validation") {
            it("should emit event with validationCode") {
                Mockito.`when`(validationCodeFactory.generateValidationCode()).thenReturn("456")
                AggregateTestFixture(UserAggregate::class.java)
                        .registerInjectableResource(validationCodeFactory)
                        .given(UserCreatedEvent(id, username), UserRegisteredEmailEvent(id, Email(address), validationCode),
                                UserValidatedEmailEvent(id))
                        .`when`(UserRegisterEmailCommand(id, "other@email.domain"))
                        .expectEvents(UserRegisteredEmailEvent(id, Email("other@email.domain"), "456"))
            }
        }

    }
})