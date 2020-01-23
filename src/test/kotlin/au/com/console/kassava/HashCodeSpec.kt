package au.com.console.kassava

import au.com.console.kassava.model.Animal
import au.com.console.kassava.model.Company
import au.com.console.kassava.model.Employee
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

/**
 * Specification for the [kotlinHashCode] extension method.
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
class HashCodeSpec : Spek({

    describe("a person") {

        val person = Employee(name = "Jim", age = 31)

        it("should have same hash as the same person object") {
            assertThat(person.hashCode(), equalTo(person.hashCode()))
        }

        it("should have same hash as a person with same name and age") {
            assertThat(person.hashCode(), equalTo(Employee(name = "Jim", age = 31).hashCode()))
        }

        it("should not have same hash as a person with different name") {
            assertThat(person.hashCode(), !equalTo(Employee(name = "Jill", age = 31).hashCode()))
        }

        it("should not have same hash as a person with different age") {
            assertThat(person.hashCode(), !equalTo(Employee(name = "Jim", age = 42).hashCode()))
        }

        it("should not have same hash as a person with null age") {
            assertThat(person.hashCode(), !equalTo(Employee(name = "Jim").hashCode()))
        }
    }

    describe("a company with employees (array scenario)") {

        val company = Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))

        it("should have same hash as a company with the same name and same array of employees") {
            val otherCompany = Company(name = "ACME", employees = company.employees)
            assertThat(company.hashCode(), equalTo(otherCompany.hashCode()))
        }

        it("should have same hash as a company with the same name and new array of the same employees (deep equals)") {
            val otherCompany = Company(name = "ACME", employees = company.employees.copyOf())
            assertThat(company.hashCode(), equalTo(otherCompany.hashCode()))
        }

        it("should have same hash as a company with the same name and new array of similar employees (deep equals)") {
            val otherCompany = Company(name = "ACME", employees = arrayOf(Employee(name = "Jim"), Employee(name = "Alice")))
            assertThat(company.hashCode(), equalTo(otherCompany.hashCode()))
        }

        it("should not have same hash as a company with the same name and slightly different employees") {
            val otherCompany = Company(name = "ACME", employees = arrayOf(Employee(name = "James"), Employee(name = "Alice")))
            assertThat(company.hashCode(), !equalTo(otherCompany.hashCode()))
        }
    }

    describe("a cat (polymorphic scenario)") {

        val cat = Animal.Cat(name = "Felix", mice = 2)

        it("should have the same hashCode as an identical cat") {
            val other = Animal.Cat(name = "Felix", mice = 2)
            assertThat(cat.hashCode(), equalTo(other.hashCode()))
        }

        it("should have a different hashCode to a cat with a different name") {
            val other = Animal.Cat(name = "Felixio", mice = 2)
            assertThat(cat.hashCode(), !equalTo(other.hashCode()))
        }

        it("should have a different hashCode to a cat with a different no of mice") {
            val other = Animal.Cat(name = "Felix", mice = 3)
            assertThat(cat.hashCode(), !equalTo(other.hashCode()))
        }

        it("should have a different hashCode to a dog with the same name") {
            val other = Animal.Dog(name = "Felix", bones = 2)
            assertThat(cat.hashCode(), !equalTo(other.hashCode()))
        }
    }
})