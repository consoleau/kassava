package au.com.console.kassava

import java.util.Objects
import kotlin.reflect.KProperty1

/**
 * Extension functions for Any.
 *
 * @author James Bassett (james.bassett@console.com.au)
 */

/**
 * Checks the other object for equality with this one, based on the supplied properties.
 *
 * @param other the other object to test for equality with this object
 * @param properties the list of properties to use when calculating equality
 * @param superEquals lambda for calling super.equals() if required (if used, all classes involved should implement the [SupportsMixedTypeEquality] interface)
 * @param T the type of the receiving class
 */
inline fun <reified T : Any> T.kotlinEquals(other: Any?,
    properties: Array<out KProperty1<T, Any?>>,
    noinline superEquals: (() -> Boolean)? = null): Boolean {
    return when {
        other === this -> true
        other !is T -> false
        other is SupportsMixedTypeEquality && !other.canEqual(this) -> false
        superEquals != null && !superEquals() -> false
        else -> {
            for (property in properties) {
                val value = property.get(this)
                val otherValue = property.get(other)
                val areEquals = if (value is Array<*>) {
                    Objects.deepEquals(value, otherValue)
                } else {
                    value === otherValue || (value != null && value == otherValue)
                }
                if (!areEquals) return false
            }
            true
        }
    }
}

/**
 * Generates the hash of an object, based on the supplied properties.
 *
 * @param properties the list of properties to include
 * @param superHashCode lambda for calling super.hashCode() if required
 * @param T the type of the receiving class
 */
inline fun <reified T : Any> T.kotlinHashCode(properties: Array<out KProperty1<T, Any?>>, noinline superHashCode: (() -> Int)? = null): Int {
    var result = 1
    for (property in properties) {
        val value = property.get(this)
        val hash = when (value) {
            null -> 0
            is Array<*> -> value.contentDeepHashCode()
            else -> value.hashCode()
        }
        result = 31 * result + hash
    }

    if (superHashCode != null) {
        result = 31 * result + superHashCode()
    }
    return result
}


/**
 * Generates the String representation of an object, based on the supplied properties.
 *
 * The implementation is based on the implementation provided by Guava's ToStringHelper (https://github.com/google/guava/wiki/CommonObjectUtilitiesExplained).
 *
 * @param properties the list of properties to include
 * @param omitNulls whether to exclude null values
 * @param superToString lambda for calling super.toString() if required
 * @param T the type of the receiving class
 */
inline fun <reified T : Any> T.kotlinToString(properties: Array<out KProperty1<T, Any?>>,
    omitNulls: Boolean = false,
    noinline superToString: (() -> String)? = null): String {

    val builder = StringBuilder(128).append(T::class.java.simpleName).append("(")

    for (property in properties) {
        val value = property.get(this)
        if (omitNulls && value == null) continue

        builder.append(property.name).append("=")
        if (value is Array<*>) {
            val arrayAsString = value.contentDeepToString()
            builder.append(arrayAsString)
        } else {
            builder.append(value)
        }
        builder.append(", ")
    }

    if (superToString == null) {
        builder.setLength(builder.length - 2) // Nothing more to add so remove the last ", " (2 is the separator length)
    } else {
        builder.append("super=").append(superToString())
    }

    return builder.append(")").toString()
}
