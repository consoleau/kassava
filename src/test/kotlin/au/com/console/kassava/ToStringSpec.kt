package au.com.console.kassava

import au.com.console.kassava.model.Animal
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * Specification for the [kotlinToString] extension method.
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class ToStringSpec : Spek({

    describe("a fully populated person") {
        val person = Person(
            name = "Jim",
            age = 31,
            address = Person.Address(
                streetNumber = 123,
                streetName = "Sesame",
                country = "US"
            )
        )
        it("should have the correct string representation") {
            assertThat(person.toString(), equalTo("Person(name=Jim, age=31, address=Address(streetNumber=123, streetName=Sesame, country=US))"))
        }
    }

    describe("a person with only mandatory properties") {
        val person = Person(
            name = "Jim",
            address = Person.Address(country = "US")
        )

        it("should have the correct string representation") {
            assertThat(person.toString(), equalTo("Person(name=Jim, age=null, address=Address(streetNumber=null, streetName=null, country=US))"))
        }
    }

    describe("a person with omitNulls enabled and only mandatory properties") {
        val person = PersonOmitNulls(
            name = "Jim",
            address = PersonOmitNulls.Address(country = "US")
        )

        it("should have the correct string representation") {
            assertThat(person.toString(), equalTo("PersonOmitNulls(name=Jim, address=Address(country=US))"))
        }
    }

    describe("an anonymous person object") {
        val person = object : Person(
            name = "Jim",
            age = 31,
            address = Person.Address(
                streetNumber = 123,
                streetName = "Sesame",
                country = "US"
            )
        ) {}
        it("should have the correct string representation") {
            assertThat(person.toString(), equalTo("Person(name=Jim, age=31, address=Address(streetNumber=123, streetName=Sesame, country=US))"))
        }
    }

    describe("a cat that extends animal") {
        val cat = Animal.Cat(name = "Marmalade", mice = 1)

        it("should have the correct string representation (with super field)") {
            assertThat(cat.toString(), equalTo("Cat(mice=1, super=Animal(name=Marmalade))"))
        }
    }

    describe("a dog that extends animal") {
        val dog = Animal.Dog(name = "Fido", bones = 2)

        it("should have the correct string representation (with super field)") {
            assertThat(dog.toString(), equalTo("Dog(bones=2, balls=null, super=Animal(name=Fido))"))
        }
    }

    describe("an object with a 2D array") {
        val value = ClassWithArray(array = arrayOf(
            arrayOf(1, 2, 3),
            null,
            arrayOf(4, 5, 6),
            arrayOf(7, 8, 9)
        ))
        it("should have the correct string representation") {
            assertThat(value.toString(), equalTo("ClassWithArray(array=[[1, 2, 3], null, [4, 5, 6], [7, 8, 9]])"))
        }
    }

})

/**
 * Simple Person class.
 */
private open class Person(val name: String, val age: Int? = null, val address: Address) {

    override fun toString() = kotlinToString(
        properties = arrayOf(Person::name, Person::age, Person::address)
    )

    class Address(val streetNumber: Int? = null, val streetName: String? = null, val country: String) {
        override fun toString() = kotlinToString(
            properties = arrayOf(Address::streetNumber, Address::streetName, Address::country)
        )
    }
}

/**
 * Person class that omits nulls in it's string representation.
 */
private class PersonOmitNulls(val name: String, val age: Int? = null, val address: Address) {

    override fun toString() = kotlinToString(
        properties = arrayOf(PersonOmitNulls::name, PersonOmitNulls::age, PersonOmitNulls::address),
        omitNulls = true
    )

    class Address(val streetNumber: Int? = null, val streetName: String? = null, val country: String) {
        override fun toString() = kotlinToString(
            properties = arrayOf(Address::streetNumber, Address::streetName, Address::country),
            omitNulls = true
        )
    }
}

/**
 * A class with a 2d array.
 */
private class ClassWithArray(val array: Array<Array<Int>?>) {
    override fun toString() = kotlinToString(properties = arrayOf(ClassWithArray::array))
}
