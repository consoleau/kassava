# Kassava
[![Build Status](https://travis-ci.org/consoleau/kassava.svg?branch=master)](https://travis-ci.org/consoleau/kassava)
[![License](http://img.shields.io/:license-apache-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)
[ ![Download](https://api.bintray.com/packages/consoleau/kotlin/kassava/images/download.svg) ](https://bintray.com/consoleau/kotlin/kassava/_latestVersion)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

This library provides some useful kotlin extension functions for implementing `toString()`, `equals()`, and `hashCode()` without all of the boilerplate.

The main motivation for this library was for situations where you can't use data classes and are required to implement `toString()`/`equals()`/`hashCode()` by:
* hand-crafting your own :(
* using the IDE generated methods :(
* using Apache Common's [ToStringBuilder](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/builder/ToStringBuilder.html) and [EqualsBuilder](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/builder/EqualsBuilder.html)
  * customizable `toString()` format (can replicate Kotlin's data class format)
  * `reflectionEquals()` and `reflectionToString()` are super easy, but have [awful performance](https://antoniogoncalves.org/2015/06/30/who-cares-about-tostring-performance/)
  * normal builders are still easy, but require lots of boilerplate
* using Guava's [MoreObjects](http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/base/MoreObjects.html)
  * `toStringBuilder()` performs better than Apache, but still requires the same boilerplate, and the format is different to the data class format (it uses braces instead of parentheses)
  * there's no equivalent builder for `equals()` (you're meant to use Java's `Objects.equals()` but that's lots of boilerplate)
  * it's a large library (2MB+) if you're not already using it
* or...something else!

Kassava provides extension functions that you can use to write your `equals()`, `hashCode()`, and `toString()` methods with no boilerplate (using the `kotlinEquals()`, `kotlinHashCode()`, and `kotlinToString()` methods respectively). While implementing `hashCode()` is trivial thanks to `java.util.Objects.hash()`, `kotlinHashCode()` allows one to pass in a list of properties which was probably already created for the `kotlinEquals()` method anyway and ensure `equals()` and `hashCode()` are both based on the same set of properties, which also makes it easy to ensure the contract between them is held up.

It's also really tiny (about 6kB), as it doesn't depend on any other libraries (like Apache Commons, or Guava). A special shoutout to Guava is required though, as the implementation of `kotlinToString()` is based heavily on the logic in [Guava's](https://github.com/google/guava/wiki/CommonObjectUtilitiesExplained) excellent `ToStringHelper`.

**How does it perform?** Check the [benchmark](#benchmarking) results below!

# Quick Start

```groovy
repositories {
    jcenter()
}

dependencies {
    compile("au.com.console:kassava:1.0.0")
}
```

# Simple Example

```kotlin
// 1. Import extension functions
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinToString
import au.com.console.kassava.kotlinHashCode

class Employee(val name: String, val age: Int? = null) {

    // 2. Optionally define your properties for equals/toString in a companion object
    // (Kotlin will generate less KProperty classes, and you won't have array creation for every method call)
    companion object {
        private val properties = arrayOf(Employee::name, Employee::age)
    }

    // 3. Implement equals() by supplying the list of properties used to test equality
    override fun equals(other: Any?) = kotlinEquals(other = other, properties = properties)

    // 4. Implement toString() by supplying the list of properties to be included
    override fun toString() = kotlinToString(properties = properties)

    // 5. Implement hashCode() by supplying the list of properties to be included
    override fun hashCode() = Objects.hash(properties = properties)
}
```

# Polymorphic Example

Implementing `equals()` with polymorphic classes (mixed type equality) is rarely done correctly, and is often the subject of heavy debate!
 
For a thorough explanation of the problem and possible solutions, please refer to both 
Angelika Langer's [Secret of equals()](http://www.angelikalanger.com/Articles/JavaSolutions/SecretsOfEquals/Equals.html) article,
and Artima's [How to Write an Equality Method in Java](http://www.artima.com/lejava/articles/equality.html) article.

In a nutshell, using `instanceof` is too lenient (and leads to implementations of `equals()` that are not transitive), 
and using `getClass()` is too strict (classes that extend in a trivial way without adding fields are no longer candidates for equality).

Kassava supports a solution that originated in the Scala world, and is proposed in the linked Artima article (and also implemented in lombok for those interested). 
The implementation makes use of a new interface `SupportsMixedTypeEquality` with a `canEquals()` method to achieve this. The purpose of this method is, as stated in the Artima article:
 
    ...as soon as a class redefines equals (and hashCode), it should also explicitly state that objects of 
    this class are never equal to objects of some superclass that implement a different equality method. 
    This is achieved by adding a method canEqual to every class that redefines equals.
    
Take a look at the unit tests for the `kotlinEquals()` method, and you'll see how the Point/ColouredPoint/anonymous Point scenario in the article works (and how
classes that extend in trivial ways - with no extra fields - can still be considered equal with this method).
    
Here is an example of it in use (in a typical kotlin sealed class example). 

Note the use of `superEquals`, `superToString`, and `superHashCode` in the subclasses - these are lambdas that allow you to reuse the logic in your parent class.

```kotlin
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinToString
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.SupportsMixedTypeEquality

/**
 * Animal base class with Cat/Dog subclasses.
 */
private sealed class Animal(val name: String) : SupportsMixedTypeEquality { // implements interface!

    override fun equals(other: Any?) = kotlinEquals(
            other = other,
            properties = arrayOf(Animal::name)
    )

    // only Animals can be compared to Animals
    override fun canEqual(other: Any?) = other is Animal

    override fun toString() = kotlinToString(properties = arrayOf(Animal::name))

    override fun hashCode() = kotlinHashCode(properties = arrayOf(Animal::name))


    class Cat(name: String, val mice: Int) : Animal(name = name){

        override fun equals(other: Any?) = kotlinEquals(
                other = other,
                properties = arrayOf(Cat::mice),
                superEquals = { super.equals(other) }
        )

        // only Cats can be compared to Cats
        override fun canEqual(other: Any?) = other is Cat

        override fun toString() = kotlinToString(
                properties = arrayOf(Cat::mice),
                superToString = { super.toString() }
        )

        override fun hashCode() = kotlinHashCode(
                properties = arrayOf(Cat::mice),
                superHashCode = { super.hashCode() }
        )
    }


    class Dog(name: String, val bones: Int) : Animal(name = name){

        override fun equals(other: Any?) = kotlinEquals(
                other = other,
                properties = arrayOf(Dog::bones),
                superEquals = { super.equals(other) }
        )

        // only Dogs can be compared to Dogs
        override fun canEqual(other: Any?) = other is Dog

        override fun toString() = kotlinToString(
                properties = arrayOf(Dog::bones),
                superToString = { super.toString() }
        )

        override fun hashCode() = kotlinHashCode(
                properties = arrayOf(Dog::bones),
                superHashCode = { super.hashCode() }
        )

    }
}
```

# Benchmarking

While Kassava's usage is very readable and maintainable, how does it perform against the alternatives?

A [Kassava JMH benchmark project](https://github.com/consoleau/kassava-benchmarks) was created to test this. You can see the [test class](https://github.com/consoleau/kassava-benchmarks/blob/master/src/main/kotlin/au/com/console/kassava/benchmark/Person.kt) implements all the variations of `toString()` and `equals()`, including:
* normal implementation (boring old IDE-generated style)
* `Objects` implementation (same as above, but with Java's `Objects.equals()` and `Objects.toString()`)
* Apache implementation
* Apache reflection implementation
* Guava implementation (for `toString()` only, there's no equivalent for `equals()`
* Kassava implementation (with reused properties array)
* Kassava implementation (with new array of properties each time)

The benchmark (using Kassava 1.0.0) was run on Travis with 10 warmup iterations, 5 test iterations, 1 fork, and measuring average time in nanoseconds. The raw results of the benchmark are:

```
Benchmark                                           Mode  Cnt     Score    Error  Units
EqualsBenchmark.apacheEquals                        avgt    5     5.365 ±  2.447  ns/op
EqualsBenchmark.apacheReflectionEquals              avgt    5   569.729 ±  5.990  ns/op
EqualsBenchmark.kassavaEquals                       avgt    5    84.647 ±  0.429  ns/op
EqualsBenchmark.kassavaEqualsWithArrayCreation      avgt    5    87.274 ±  0.520  ns/op
EqualsBenchmark.manualEquals                        avgt    5     5.665 ±  0.081  ns/op
EqualsBenchmark.manualObjectsEquals                 avgt    5     6.866 ±  0.042  ns/op
ToStringBenchmark.apacheReflectionToString          avgt    5  1484.542 ± 28.615  ns/op
ToStringBenchmark.apacheToString                    avgt    5   922.272 ± 52.431  ns/op
ToStringBenchmark.guavaToString                     avgt    5   344.156 ±  6.403  ns/op
ToStringBenchmark.kassavaToString                   avgt    5   416.654 ± 10.255  ns/op
ToStringBenchmark.kassavaToStringWithArrayCreation  avgt    5   420.433 ±  7.425  ns/op
ToStringBenchmark.manualObjectsToString             avgt    5   140.707 ±  2.457  ns/op
ToStringBenchmark.manualToString                    avgt    5   118.061 ±  2.196  ns/op
```

TLDR:
* Kassava's `equals()` implementation is definitely slower than manual/Apache/Guava (approx 15x slower), but nowhere near as bad as Apache's reflection implementation (approx 100x slower)
* Kassava's `toString()` implementation is only slightly slower than Guava (and faster than Apache!)
* Apache's `equals()` implementation is faster than the manual implementation??? (Magic?)

# Contributing to the Project #

If you'd like to contribute code to this project you can do so through GitHub by forking the repository and generating a pull request.

By contributing your code, you agree to license your contribution under the terms of the Apache License v2.0. 

# License #

Copyright 2016 RES INFORMATION SERVICES PTY LTD

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
