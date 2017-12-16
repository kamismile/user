package nl.spelberg.microservice.user.types

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class UserTypesTest : Spek({
    describe("Email") {
        on("correct email") {
            it("should create") {
                assertThat(Email("chris@spelberg.nl").address, equalTo("chris@spelberg.nl"))
            }
        }
        on("incorrect email") {
            it("should throw") {
                assertThrows(IllegalArgumentException("Invalid email address: chris.spelberg.nl")) {
                    Email("chris.spelberg.nl")
                }
            }
        }
    }
})

fun <T: Throwable> assertThrows(throwableClass: Class<T>, block: () -> Unit) {
    try {
        block()
        throw AssertionError("Expected to throw ${throwableClass.name}")
    } catch (t: Throwable) {
        if (!throwableClass.isAssignableFrom(t.javaClass)) {
            throw AssertionError("Expected to throw ${throwableClass.name}")
        }
    }
}

fun <T: Throwable> assertThrows(expected: T, block: () -> Unit) {
    try {
        block()
        throw AssertionError("Expected to throw $expected")
    } catch (t: Throwable) {
        val throwableClass = expected.javaClass
        if (!throwableClass.isAssignableFrom(t.javaClass)) {
            throw AssertionError("Expected to throw $expected")
        }
        assertThat(t.message, equalTo(expected.message))
    }
}