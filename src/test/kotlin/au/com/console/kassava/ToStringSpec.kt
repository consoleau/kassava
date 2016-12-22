package au.com.console.kassava

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import java.util.*

/**
 * Specification for the [kotlinToString] extension method.
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class ToStringSpec : Spek({

    given("a fully populated person"){
        val person = Person(
                name = "Jim",
                age = 31,
                address = Person.Address(
                        streetNumber = 123,
                        streetName = "Sesame",
                        country = "US"
                )
        )
        it("should have the correct string representation"){
            assertThat(person.toString(), equalTo("Person(name=Jim, age=31, address=Address(streetNumber=123, streetName=Sesame, country=US))"))
        }
    }

    given("a person with only mandatory properties"){
        val person = Person(
                name = "Jim",
                address = Person.Address(country = "US")
        )

        it("should have the correct string representation"){
            assertThat(person.toString(), equalTo("Person(name=Jim, age=null, address=Address(streetNumber=null, streetName=null, country=US))"))
        }
    }

    given("a person with omitNulls enabled and only mandatory properties"){
        val person = PersonOmitNulls(
                name = "Jim",
                address = PersonOmitNulls.Address(country = "US")
        )

        it("should have the correct string representation"){
            assertThat(person.toString(), equalTo("PersonOmitNulls(name=Jim, address=Address(country=US))"))
        }
    }

    given("an anonymous person object"){
        val person = object : Person(
                name = "Jim",
                age = 31,
                address = Person.Address(
                        streetNumber = 123,
                        streetName = "Sesame",
                        country = "US"
                )
        ){}
        it("should have the correct string representation"){
            assertThat(person.toString(), equalTo("Person(name=Jim, age=31, address=Address(streetNumber=123, streetName=Sesame, country=US))"))
        }
    }

    given("a cat that extends animal"){
        val cat = Animal.Cat(name = "Marmalade", mice = 1)

        it("should have the correct string representation (with super field)"){
            assertThat(cat.toString(), equalTo("Cat(mice=1, super=Animal(name=Marmalade))"))
        }
    }

    given("a dog that extends animal"){
        val dog = Animal.Dog(name = "Fido", bones = 2)

        it("should have the correct string representation (with super field)"){
            assertThat(dog.toString(), equalTo("Dog(bones=2, super=Animal(name=Fido))"))
        }
    }

    given("an object with a 2D array"){
        val value = ClassWithArray(array = arrayOf(
                arrayOf(1, 2, 3),
                null,
                arrayOf(4, 5, 6),
                arrayOf(7, 8, 9)
        ))
        it("should have the correct string representation"){
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

    class Address(val streetNumber: Int? = null, val streetName: String? = null, val country: String){
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

    class Address(val streetNumber: Int? = null, val streetName: String? = null, val country: String){
        override fun toString() = kotlinToString(
                properties = arrayOf(Address::streetNumber, Address::streetName, Address::country),
                omitNulls = true
        )
    }
}

/**
 * Animal base class with Cat/Dog subclasses.
 */
private sealed class Animal(val name: String) : SupportsMixedTypeEquality {

    override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = arrayOf(Animal::name)
    )

    override fun canEqual(other: Any?) = other is Animal

    override fun toString() = kotlinToString(properties = arrayOf(Animal::name))

    override fun hashCode() = Objects.hash(name)


    class Cat(name: String, val mice: Int) : Animal(name = name){

        override fun equals(other: Any?) = kotlinEquals(
                other = other,
                properties = arrayOf(Cat::mice),
                superEquals = { super.equals(other) }
        )

        override fun canEqual(other: Any?) = other is Cat

        override fun toString() = kotlinToString(
                properties = arrayOf(Cat::mice),
                superToString = { super.toString() }
        )

        override fun hashCode() = Objects.hash(mice, super.hashCode())
    }


    class Dog(name: String, val bones: Int) : Animal(name = name){

        override fun equals(other: Any?) = kotlinEquals(
                other = other,
                properties = arrayOf(Dog::bones),
                superEquals = { super.equals(other) }
        )

        override fun canEqual(other: Any?) = other is Dog

        override fun toString() = kotlinToString(
                properties = arrayOf(Dog::bones),
                superToString = { super.toString() }
        )

        override fun hashCode() = Objects.hash(bones, super.hashCode())

    }

}

/**
 * A class with a 2d array.
 */
private class ClassWithArray(val array: Array<Array<Int>?>){
    override fun toString() = kotlinToString(properties = arrayOf(ClassWithArray::array))
}
