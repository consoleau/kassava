package au.com.console.kassava

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.*

/**
 * Specification for the [kotlinEquals] extension method.
 *
 * The multi-type equality example (Point, ColouredPoint, etc) is taken from the excellent
 * Artima article at http://www.artima.com/lejava/articles/equality.html
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class EqualsSpec : Spek({

    given("a person"){

        val person = Employee(name = "Jim", age = 31)

        it("should be equal to the same person object"){
            assertThat(person, equalTo(person))
        }

        it("should not be equal to null"){
            assertThat(person, !equalTo<Employee>(null))
        }

        it("should not be equal to another type"){
            assertThat(person, !equalTo<Any>("person"))
        }

        it("should be equal to a person with same name and age"){
            assertThat(person, equalTo(Employee(name = "Jim", age = 31)))
        }

        it("should not be equal to a person with different name"){
            assertThat(person, !equalTo(Employee(name = "Jill", age = 31)))
        }

        it("should not be equal to a person with different age"){
            assertThat(person, !equalTo(Employee(name = "Jim", age = 42)))
        }

        it("should not be equal to a person with null age"){
            assertThat(person, !equalTo(Employee(name = "Jim")))
        }
    }

    given("a point, coloured point and anonymous point") {

        val point = Point(x = 1, y = 2)
        val colouredPoint = ColouredPoint(1, 2, "INDIGO")
        val anonymousPoint = object : Point(x = 1, y = 1) {
            override val y = 2
        }

        on("adding the point to a hash set") {
            val set = HashSet<Point>()
            set.add(point)

            it("should only contain the point and anonymous point") {
                assertThat(set, hasElement(point))
                assertThat(set, !hasElement<Point>(colouredPoint))
                assertThat(set, hasElement<Point>(anonymousPoint))
            }
        }

        on("comparing the point to the coloured point"){
            it("should be false") {
                assertThat(point, !equalTo<Point>(colouredPoint))
                assertThat(colouredPoint, !equalTo(point))
            }
        }

        on("comparing the point to the anonymous point"){
            it("should be true - because anonymous point doesn't override canEqual()") {
                assertThat(point, equalTo<Point>(anonymousPoint))
                assertThat(anonymousPoint, equalTo(point))
            }
        }

        on("comparing the coloured point to the anonymous point"){
            it("should be false") {
                assertThat(colouredPoint, !equalTo<Point>(anonymousPoint))
                assertThat(anonymousPoint, !equalTo<Point>(colouredPoint))
            }
        }
    }
})

/**
 * Simple Employee class.
 */
private class Employee(val name: String, val age: Int? = null) {

    companion object {
        private val properties = arrayOf(Employee::name, Employee::age)
    }

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = properties)

    override fun toString() = kotlinToString(properties = properties)

    override fun hashCode() = Objects.hash(name, age)
}

/**
 * Point base class that supports mixed type equality (subclasses must override
 * the canEqual() method if they add any fields in order to preserve the equals contract).
 */
private open class Point(open val x: Int, open val y: Int) : SupportsMixedTypeEquality {

    override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = arrayOf(Point::x, Point::y)
    )

    override fun toString() = kotlinToString(properties = arrayOf(Point::x, Point::y))

    override fun hashCode() = Objects.hash(x, y)

    override fun canEqual(other: Any?) = other is Point
}

/**
 * ColouredPoint subclass that overrides canEqual().
 */
private class ColouredPoint(x: Int, y: Int, val colour: String) : Point(x, y) {

    override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = arrayOf(ColouredPoint::colour),
            superEquals = { super.equals(other) }
    )

    override fun toString() = kotlinToString(
            properties = arrayOf(ColouredPoint::colour),
            superToString = { super.toString() }
    )

    override fun hashCode() = Objects.hash(colour, super.hashCode())

    override fun canEqual(other: Any?) = other is ColouredPoint
}

