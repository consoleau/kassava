package au.com.console.kassava.model

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString

/**
 * Company class - for array field equality.
 */
class Company(val name: String, val employees: Array<Employee>){

    companion object {
        private val properties = arrayOf(Company::name, Company::employees)
    }

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = properties)

    override fun toString() = kotlinToString(properties = properties)

    override fun hashCode() = kotlinHashCode(properties = properties)

}

/**
 * Simple Employee class.
 */
class Employee(val name: String, val age: Int? = null) {

    companion object {
        private val properties = arrayOf(Employee::name, Employee::age)
    }

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = properties)

    override fun toString() = kotlinToString(properties = properties)

    override fun hashCode() = kotlinHashCode(properties = properties)
}