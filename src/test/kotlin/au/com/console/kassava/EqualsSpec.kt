package au.com.console.kassava

import au.com.console.kassava.model.ColouredPoint
import au.com.console.kassava.model.Company
import au.com.console.kassava.model.Employee
import au.com.console.kassava.model.Point
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.HashSet

/**
 * Specification for the [kotlinEquals] extension method.
 *
 * The multi-type equality example (Point, ColouredPoint, etc) is taken from the excellent
 * Artima article at http://www.artima.com/lejava/articles/equality.html
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class EqualsSpec : Spek({

    describe("a person") {

        val person = Employee(name = "Jim", age = 31)

        it("should be equal to the same person object") {
            assertThat(person, equalTo(person))
        }

        it("should not be equal to null") {
            assertThat(person, !equalTo<Employee>(null))
        }

        it("should not be equal to another type") {
            assertThat(person, !equalTo<Any>("person"))
        }

        it("should be equal to a person with same name and age") {
            assertThat(person, equalTo(Employee(name = "Jim", age = 31)))
        }

        it("should not be equal to a person with different name") {
            assertThat(person, !equalTo(Employee(name = "Jill", age = 31)))
        }

        it("should not be equal to a person with different age") {
            assertThat(person, !equalTo(Employee(name = "Jim", age = 42)))
        }

        it("should not be equal to a person with null age") {
            assertThat(person, !equalTo(Employee(name = "Jim")))
        }
    }

    describe("a point, coloured point and anonymous point") {

        val point = Point(x = 1, y = 2)
        val colouredPoint = ColouredPoint(1, 2, "INDIGO")
        val anonymousPoint = object : Point(x = 1, y = 1) {
            override val y = 2
        }

        describe("adding the point to a hash set") {
            val set = HashSet<Point>()
            set.add(point)

            it("should only contain the point and anonymous point") {
                assertThat(set, hasElement(point))
                assertThat(set, !hasElement<Point>(colouredPoint))
                assertThat(set, hasElement<Point>(anonymousPoint))
            }
        }

        describe("comparing the point to the coloured point") {
            it("should be false") {
                assertThat(point, !equalTo<Point>(colouredPoint))
                assertThat(colouredPoint, !equalTo(point))
            }
        }

        describe("comparing the point to the anonymous point") {
            it("should be true - because anonymous point doesn't override canEqual()") {
                assertThat(point, equalTo<Point>(anonymousPoint))
                assertThat(anonymousPoint, equalTo(point))
            }
        }

        describe("comparing the coloured point to the anonymous point") {
            it("should be false") {
                assertThat(colouredPoint, !equalTo<Point>(anonymousPoint))
                assertThat(anonymousPoint, !equalTo<Point>(colouredPoint))
            }
        }
    }

    describe("a company with employees") {

        val company = Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))

        it("should be equal to a company with the same name and same array of employees") {
            val otherCompany = Company(name = "ACME", employees = company.employees)
            assertThat(company, equalTo(otherCompany))
        }

        it("should be equal to a company with the same name and new array of the same employees (deep equals)") {
            val otherCompany = Company(name = "ACME", employees = company.employees.copyOf())
            assertThat(company, equalTo(otherCompany))
        }

        it("should be equal to a company with the same name and new array of similar employees (deep equals)") {
            val otherCompany = Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))
            assertThat(company, equalTo(otherCompany))
        }

        it("should not be equal to a company with the same name and slightly different employees") {
            val otherCompany = Company(name = "ACME", employees = arrayOf(Employee(name = "James"), Employee(name = "Alice")))
            assertThat(company, !equalTo(otherCompany))
        }
    }
})

