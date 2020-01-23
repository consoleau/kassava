package au.com.console.kassava.model

import au.com.console.kassava.SupportsMixedTypeEquality
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString

/**
 * Animal base class with Cat/Dog subclasses.
 */
sealed class Animal(val name: String) : SupportsMixedTypeEquality {

    override fun equals(other: Any?) = kotlinEquals(
        other = other,
        properties = properties
    )

    // only Animals can be compared to Animals
    override fun canEqual(other: Any?) = other is Animal

    override fun toString() = kotlinToString(properties = properties)

    override fun hashCode() = kotlinHashCode(properties = properties)

    companion object {
        private val properties = arrayOf(Animal::name)
    }

    class Cat(name: String, val mice: Int) : Animal(name = name) {
        
        override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = properties,
            superEquals = { super.equals(other) }
        )

        // only Cats can be compared to Cats
        override fun canEqual(other: Any?) = other is Cat

        override fun toString() = kotlinToString(
            properties = properties,
            superToString = { super.toString() }
        )

        override fun hashCode() = kotlinHashCode(
            properties = properties,
            superHashCode = { super.hashCode() }
        )

        companion object {
            private val properties = arrayOf(Cat::mice)
        }
    }

    class Dog(name: String, val bones: Int, val balls: Int? = null) : Animal(name = name) {

        override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = properties,
            superEquals = { super.equals(other) }
        )

        // only Dogs can be compared to Dogs
        override fun canEqual(other: Any?) = other is Dog

        override fun toString() = kotlinToString(
            properties = properties,
            superToString = { super.toString() }
        )

        override fun hashCode() = kotlinHashCode(
            properties = properties,
            superHashCode = { super.hashCode() }
        )

        companion object {
            private val properties = arrayOf(Dog::bones, Dog::balls)
        }
    }
}