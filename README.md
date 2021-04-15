# vavr-matchers

![vavr-matchers](https://github.com/agrison/vavr-matchers/workflows/vavr-matchers/badge.svg)

This library contains matchers for hamcrest.

## Install

```xml
<dependency>
  <groupId>me.grison</groupId>
  <artifactId>vavr-matchers</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage

```java
import static me.grison.vavr.matchers.VavrMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class AllTests {
    @Test
    public void testTry() {
        Try<Integer> age = Try.of(() -> 30);

        // ensure the Try is a success and its value is less than 40
        assertThat(age, isSuccess(lessThan(40)));
    }
    
    @Test
    public void testTraversable() {
        List<Integer> ages = List.of(28, 35, 36, 40);

        // ensure not empty
        assertThat(ages, not(isEmpty()));

        // ensure length is 4
        assertThat(ages, hasLength(4));

        // ensure it contains 35
        assertThat(ages, contains(35));

        // ensure it contains at least a value less than 30
        assertThat(ages, contains(lessThan(30)));

        // ensure that all values are less than 50
        assertThat(ages, allMatch(lessThan(50)));
    }
}
```

See below for all available matchers.

## Matchers

### Option

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isDefined()     | Verifies that an `Option` is defined |
| isDefined(Matcher)     | Verifies that an `Option` is defined and its content matches a `Matcher` |
| isEmpty()     | Verifies that an `Option` is undefined |

### Try

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isSuccess()     | Verifies that a `Try` is a `Success` |
| isSuccess(Matcher)     | Verifies that a `Try` is a `Success` and its content matches a `Matcher` |
| isFailure()     | Verifies that a `Try` is a `Failure` |
| isFailure(Class&lt;E extends Throwable>)     | Verifies that a `Try` is a `Failure` and its cause is a specific `Throwable` |

### Either

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isRight()     | Verifies that an `Either` is a `Right` |
| isRight(Matcher)     | Verifies that an `Either` is a `Right` and its content matches a `Matcher` |
| isLeft()     | Verifies that an `Either` is a `Left` |
| isLeft(Matcher)     | Verifies that an `Either` is a `Left` and its content matches a `Matcher` |

### Traversable

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isEmpty()     | Verifies that a `Traversable` is empty |
| hasLength(int)     | Verifies that a `Traversable` has a specific length |
| hasLength(Matcher)     | Verifies that a `Traversable` has a length matching a `Matcher` |
| contains(T)     | Verifies that a `Traversable` contain a specific element |
| contains(Matcher)     | Verifies that a `Traversable` contain a specific element matching a `Matcher` |
| containsInAnyOrder(T...)     | Verifies that a `Traversable` contain the given elements |
| containsInAnyOrder(Traversable)     | Verifies that a `Traversable` contain the given elements |
| allMatch(Matcher)     | Verifies that a `Traversable` contain only elements matching a `Matcher` |
| isSorted()     | Verifies that a `Traversable` is sorted |
| isReverseSorted()     | Verifies that a `Traversable` is reverse sorted |
| startsWith(T...)     | Verifies that a `Traversable` starts with the given elements |
| startsWith(Traversable)     | Verifies that a `Traversable` starts with the given elements |
| endsWith(T...)     | Verifies that a `Traversable` ends with the given elements |
| endsWith(Traversable)     | Verifies that a `Traversable` ends with the given elements |
| isUnique()     | Verifies that a `Traversable` contains no duplicates |

### Map

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| containsKeys(T...)     | Verifies that a `Map` contains at least the given keys |
| containsKeys(Traversable)     | Verifies that a `Map` contains at least the given keys |
| containsValues(T...)     | Verifies that a `Map` contains at least the given values |
| containsValues(Traversable)     | Verifies that a `Map` contains at least the given values |
| contains(T key, U value)     | Verifies that a `Map` contains at least the given entry |

### Future

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isCancelled()     | Verifies that a `Future` is cancelled |
| isCompleted()     | Verifies that a `Future` is completed |
| isCompleted(Matcher)     | Verifies that a `Future` is completed and its content matches a `Matcher` |

### Lazy

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isEvaluated()     | Verifies that a `Lazy` has been evaluated |
| isEvaluated(Matcher)     | Verifies that a `Lazy` has been evaluated and its content matches a `Matcher` |

### Tuple

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| hasArity(int)     | Verifies that a `Tuple` has a specific arity |
| hasArity(Matcher)     | Verifies that a `Tuple` has a specific arity matching a `Matcher` |

### Validation

| Assertion                 | Description                                       |
|------------------------|---------------------------------------------------|
| isValid()     | Verifies that a `Validation` is valid |
| isValid(Matcher)     | Verifies that a `Validation` is valid and its content matches a `Matcher` |
| isInvalid()     | Verifies that a `Validation` is invalid |
| isInvalid(Matcher)     | Verifies that a `Validation` is invalid and its content matches a `Matcher` |

## Contribute

It is a work in progress, so don't hesitate to contribute and add more matchers.

## Thanks

I needed this library and discovered an existing one from Vincent Ambo (@tazjin): https://github.com/tazjin/vavr-matchers

Unfortunately it has been archived for years and couldn't fork it, so as I needed new functionalities here comes this library.
