# Kassava
[![Build Status](https://travis-ci.org/consoleau/kassava.svg?branch=master)](https://travis-ci.org/consoleau/kassava)
[![License](http://img.shields.io/:license-apache-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)
[ ![Download](https://api.bintray.com/packages/consoleau/kotlin/kassava/images/download.svg) ](https://bintray.com/consoleau/kotlin/kassava/_latestVersion)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

This library provides some useful kotlin extension functions for implementing `toString()` and `equals()` without all of the boilerplate.

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

Kassava provides extension functions that you can use to write your `equals()` and `toString()` methods with no boilerplate (using the `kotlinEquals()` and `kotlinToString()` methods respectively). `hashCode()` is trivial to implement now that Java has `Objects.hash()`, so there wasn't any need for improvement there.

It does not depend on any other libraries (like Apache Commons, or Guava), though the implementation of `kotlinToString()` is based heavily on the logic in [Guava's](https://github.com/google/guava/wiki/CommonObjectUtilitiesExplained) excellent `ToStringHelper`.

*Please note:* this is still a proof of concept. While the usage is very readable and maintainable, the next step is to benchmark against the alternatives to see if the solution is
production-ready.

# Quick Start

```groovy
repositories {
    jcenter()
}

dependencies {
    compile("au.com.console:kassava:0.1.0-rc.2")
}
```

# Simple Example

```kotlin
// 1. Import extension functions
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinToString

import java.util.Objects

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

    // 5. Implement hashCode() because you're awesome and know what you're doing ;)
    override fun hashCode() = Objects.hash(name, age)
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

Note the use of `superEquals` and `superToString` in the subclasses - these are lambdas that allow you to reuse the logic in your parent class.

```kotlin
import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinToString
import au.com.console.kassava.SupportsMixedTypeEquality
import java.util.Objects

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

    override fun hashCode() = Objects.hash(name)


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

        override fun hashCode() = Objects.hash(mice, super.hashCode())
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

        override fun hashCode() = Objects.hash(bones, super.hashCode())

    }
}
```

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
