package au.com.console.kassava

/**
 * Interface to enable mixed-type equality.
 * See: http://www.artima.com/lejava/articles/equality.html
 *
 * @author James Bassett (james.bassett@console.com.au)
 */
interface SupportsMixedTypeEquality {

    /**
     * Returns true if this object can be compared for equality with the other object.
     *
     * The typical implementation is to check if other is an instance of this class. This method should always be
     * overridden in subclasses, unless they are trivially different (no new fields).
     */
    fun canEqual(other: Any?): Boolean
}