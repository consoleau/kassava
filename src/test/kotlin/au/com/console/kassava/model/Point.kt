package au.com.console.kassava.model

import au.com.console.kassava.SupportsMixedTypeEquality
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import java.util.Objects

/**
 * Point base class that supports mixed type equality (subclasses must override
 * the canEqual() method if they add any fields in order to preserve the equals contract).
 */
open class Point(open val x: Int, open val y: Int) : SupportsMixedTypeEquality {

    companion object {
        private val properties = arrayOf(Point::x, Point::y)
    }
    
    override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = properties
    )

    override fun toString() = kotlinToString(properties = properties)

    override fun hashCode() = kotlinHashCode(properties = properties)

    override fun canEqual(other: Any?) = other is Point
}

/**
 * ColouredPoint subclass that overrides canEqual().
 */
class ColouredPoint(x: Int, y: Int, val colour: String) : Point(x, y) {

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