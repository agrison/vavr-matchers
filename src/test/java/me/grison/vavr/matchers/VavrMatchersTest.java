package me.grison.vavr.matchers;

import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static me.grison.vavr.matchers.VavrMatchers.contains;
import static me.grison.vavr.matchers.VavrMatchers.containsInAnyOrder;
import static me.grison.vavr.matchers.VavrMatchers.endsWith;
import static me.grison.vavr.matchers.VavrMatchers.hasLength;
import static me.grison.vavr.matchers.VavrMatchers.startsWith;
import static me.grison.vavr.matchers.VavrMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class VavrMatchersTest {
    @Test
    public void testIsDefined() {
        assertThat(Option.of("foo"), isDefined());
        assertThat(Option.none(), not(isDefined()));

        Description description = new StringDescription();
        isDefined().describeMismatch(Option.none(), description);
        assertThat(description.toString(), is("No value defined"));

        description = new StringDescription();
        isDefined(lessThan(36)).describeMismatch(Option.of(1337), description);
        assertThat(description.toString(), is("<1337> was greater than <36>"));
    }

    @Test
    public void testIsEmpty() {
        assertThat(Option.of("foo"), not(isEmpty()));
        assertThat(Option.none(), isEmpty());

        Description description = new StringDescription();
        isEmpty().describeMismatch(Option.of(1337), description);
        assertThat(description.toString(), is("Expected an empty value but found <1337>"));
    }

    @Test
    public void testIsSuccess() {
        assertThat(Try.success("foo"), isSuccess());
        assertThat(Try.of(() -> "foo"), isSuccess());
        assertThat(Try.success("foo"), isSuccess(not(emptyString())));

        Description description = new StringDescription();
        isSuccess().describeMismatch(Try.failure(new Exception()), description);
        assertThat(description.toString(),
                is("Expected a <Success> but found <Failure(java.lang.Exception)>"));

        assertThat(Try.failure(new Exception()), not(isSuccess()));
        assertThat(Try.of(() -> {
            throw new Exception();
        }), not(isSuccess()));
        assertThat(Try.failure(new Exception()), not(isSuccess(anything())));

        description = new StringDescription();
        isSuccess(lessThan(36)).describeMismatch(Try.success(1337), description);
        assertThat(description.toString(),
                is("Expected a <Success> with content matching `a value less than <36>` but <1337> was greater than <36>"));
    }

    @Test
    public void testIsFailure() {
        assertThat(Try.failure(new Exception()), isFailure());
        assertThat(Try.of(() -> {
            throw new Exception();
        }), isFailure());

        assertThat(Try.success("foo"), not(isFailure()));
        assertThat(Try.of(() -> "foo"), not(isFailure()));

        Description description = new StringDescription();
        isFailure().describeMismatch(Try.success("foo"), description);
        assertThat(description.toString(),
                is("Expected a <Failure> but found <Success(foo)>"));
    }

    @Test
    public void testIsFailureWithException() {
        assertThat(Try.failure(new IllegalStateException()), isFailure(IllegalStateException.class));
        assertThat(Try.of(() -> {
            throw new IllegalStateException();
        }), isFailure(IllegalStateException.class));

        assertThat(Try.failure(new IllegalArgumentException()), not(isFailure(IllegalStateException.class)));
        assertThat(Try.of(() -> {
            throw new IllegalArgumentException();
        }), not(isFailure(IllegalStateException.class)));

        assertThat(Try.success("foo"), not(isFailure(IllegalArgumentException.class)));
        assertThat(Try.of(() -> "foo"), not(isFailure(IllegalArgumentException.class)));

        Description description = new StringDescription();
        isFailure(IllegalArgumentException.class).describeMismatch(Try.success("foo"), description);
        assertThat(description.toString(), is("Expected <Failure()> but found <Success(foo)>"));

        description = new StringDescription();
        isFailure(IllegalStateException.class)
                .describeMismatch(Try.of(() -> {
                    throw new IllegalArgumentException();
                }), description);
        assertThat(description.toString(),
                is("Expected <Failure(java.lang.IllegalStateException)> but found <Failure(java.lang.IllegalArgumentException)>"));
    }

    @Test
    public void testIsRight() {
        assertThat(Either.right("foo"), isRight());
        assertThat(Either.left("foo"), not(isRight()));

        Description description = new StringDescription();
        isRight().describeMismatch(Either.left("foo"), description);
        assertThat(description.toString(), is("Expected a <Right> but got <Left(foo)>"));

        description = new StringDescription();
        isRight(is(1)).describeMismatch(Either.right(36), description);
        assertThat(description.toString(), is("Expected a <Right> with content matching `is <1>` but was <36>"));
    }

    @Test
    public void testIsLeft() {
        assertThat(Either.left("foo"), isLeft());
        assertThat(Either.right("foo"), not(isLeft()));

        Description description = new StringDescription();
        isLeft().describeMismatch(Either.right("foo"), description);
        assertThat(description.toString(), is("Expected a <Left> but got <Right(foo)>"));

        description = new StringDescription();
        isLeft(is(1)).describeMismatch(Either.left(36), description);
        assertThat(description.toString(), is("Expected a <Left> with content matching `is <1>` but was <36>"));
    }

    @Test
    public void testCollectionIsEmpty() {
        assertThat(List.empty(), isEmpty());
        assertThat(List.of("foo"), not(isEmpty()));

        Description description = new StringDescription();
        isEmpty().describeMismatch(List.of("foo", "bar"), description);
        assertThat(description.toString(), is("Expected an empty value but found <[foo, bar]>"));
    }

    @Test
    public void testHasLength() {
        assertThat(List.of(1, 2, 3, 4, 5), hasLength(5));
        assertThat(List.empty(), not(hasLength(5)));

        assertThat(List.of(1, 2, 3), hasLength(lessThan(20)));
        assertThat(List.of(1, 2, 3), not(hasLength(lessThan(2))));

        Description description = new StringDescription();
        hasLength(1).describeMismatch(List.of("foo", "bar"), description);
        assertThat(description.toString(),
                is("Expected Traversable to have length <1> but has length <2>"));

        description = new StringDescription();
        hasLength(lessThan(2)).describeMismatch(List.of("foo", "bar", "bazz"), description);
        assertThat(description.toString(),
                is("Expected Traversable to match length a value less than <2> but has length <3>"));
    }

    @Test
    public void testContains() {
        assertThat(List.of("foo", "bar"), contains("foo"));
        assertThat(List.of("foo", "bar"), not(contains("bazz")));
        assertThat(List.empty(), not(contains("foo")));

        assertThat(List.of("foo", "bar"), contains(is("foo")));
        assertThat(List.of("foo", "bar"), not(contains(is("bazz"))));
        assertThat(List.empty(), not(contains(is("foo"))));

        Description description = new StringDescription();
        contains(is(0)).describeMismatch(List.of(1, 2), description);
        assertThat(description.toString(),
                is("Expected at least one element matching `is <0>' but found <List(1, 2)>"));
    }

    @Test
    public void testContainsInAnyOrder() {
        assertThat(List.of("foo", "bar", "bazz"), containsInAnyOrder(List.of("bar", "foo")));
        assertThat(List.of("foo", "bar", "bazz"), containsInAnyOrder("bar", "foo"));
        assertThat(List.empty(), not(containsInAnyOrder(List.of("foo", "bar"))));

        Description description = new StringDescription();
        containsInAnyOrder(List.of("foo", "bar", "bazz")).describeMismatch(List.of("foo"), description);
        assertThat(description.toString(),
                is("Expected a Traversable containing all of [\"foo\",\"bar\",\"bazz\"] but is missing [\"bar\",\"bazz\"]"));
    }

    @Test
    public void testContainsSubList() {
        assertThat(List.of("foo", "bar", "bazz"), containsSubList(List.of("foo", "bar", "bazz")));
        assertThat(List.of("bar", "foo", "bazz"), containsSubList("bar", "foo"));
        assertThat(List.empty(), not(containsSubList(List.of("foo", "bar"))));
        assertThat(List.of("bazz", "foo", "bar"), containsSubList(List.of("foo", "bar")));

        Description description = new StringDescription();
        containsSubList(List.of("foo", "bar", "bazz")).describeMismatch(List.of("foo"), description);
        assertThat(description.toString(),
                is("Expected a Traversable containing in same order all of [\"foo\",\"bar\",\"bazz\"] but is missing [\"bar\",\"bazz\"]"));
    }

    @Test
    public void testContainsInOrder() {
        assertThat(List.of("foo", "bar", "bazz"), containsInOrder(List.of("foo", "bar")));
        assertThat(List.of("foo", "bar", "bazz"), containsInOrder(List.of("bar", "bazz")));
        assertThat(List.of("foo", "bar", "bazz", "quxx"), containsInOrder("foo", "bazz", "quxx"));
        assertThat(List.of("foo", "bar", "bazz"), not(containsInOrder("bar", "foo")));
        assertThat(List.of("foo", "bar"), not(containsInOrder("foo", "bar", "bazz")));
        assertThat(List.empty(), not(containsInOrder(List.of("foo", "bar"))));

        Description description = new StringDescription();
        containsInOrder(List.of("foo", "bar")).describeMismatch(List.of("bar", "foo", "bazz"), description);
        assertThat(description.toString(),
                is("Expected a Traversable containing in same order all of [\"foo\",\"bar\"] but was not"));
    }

    @Test
    public void testAllMatch() {
        assertThat(List.of("foo", "bar", "baz"), allMatch(Matchers.hasLength(3)));
        assertThat(List.of("foo", "bar"), not(allMatch(Matchers.hasLength(4))));

        final Description description = new StringDescription();
        allMatch(is(true)).describeMismatch(List.of(false, true, false), description);
        assertThat(description.toString(),
                is("Expected a Traversable where all elements should match is <true> but found non-matching elements [<false>,<false>]"));
    }

    @Test
    public void testIsSorted() {
        assertThat(List.of(1, 2, 3), isSorted());
        assertThat(List.of(2, 1, 4), not(isSorted()));

        Description description = new StringDescription();
        isSorted().describeMismatch(List.of(2, 1, 3), description);
        assertThat(description.toString(),
                is("Expected a Seq to be sorted but it was not"));
    }

    @Test
    public void testIsReverseSorted() {
        assertThat(List.of(3, 2, 1), isReverseSorted());
        assertThat(List.of(2, 1, 4), not(isReverseSorted()));

        Description description = new StringDescription();
        isReverseSorted().describeMismatch(List.of(2, 1, 3), description);
        assertThat(description.toString(),
                is("Expected a Seq to be reverse sorted but it was not"));
    }

    @Test
    public void testStartsWith() {
        assertThat(List.of(1, 2, 3), startsWith(List.of(1, 2)));
        assertThat(List.of(1, 2, 3), startsWith(1, 2));
        assertThat(List.of(2, 1, 4), not(startsWith(List.of(1, 2))));

        Description description = new StringDescription();
        startsWith(List.of(1, 2)).describeMismatch(List.of(2, 1, 3), description);
        assertThat(description.toString(),
                is("Expected a Seq to start with [<1>,<2>] but found a Seq starting with [<2>,<1>]"));
    }

    @Test
    public void testEndsWith() {
        assertThat(List.of(1, 2, 3), endsWith(List.of(2, 3)));
        assertThat(List.of(1, 2, 3), endsWith(2, 3));
        assertThat(List.of(2, 1, 4), not(endsWith(List.of(1, 2))));

        Description description = new StringDescription();
        endsWith(List.of(1, 2, 3)).describeMismatch(List.of(2, 1), description);
        assertThat(description.toString(),
                is("Expected a Seq to end with [<1>,<2>,<3>] but found a Seq ending with [<2>,<1>]"));
    }

    @Test
    public void testIsUnique() {
        assertThat(List.of(1, 2, 3, 4), isUnique());
        assertThat(List.of(1, 2, 3, 3), not(isUnique()));

        Description description = new StringDescription();
        isUnique().describeMismatch(List.of(1, 2, 1, 3, 3, 4), description);
        assertThat(description.toString(),
                is("Expected a Seq to have unique elements but found the following duplicate elements [<1>,<3>]"));
    }

    @Test
    public void testContainsSubSet() {
        assertThat(HashSet.of(1, 2, 3, 4), containsSubSet(1, 2));
        assertThat(HashSet.of(1, 2, 3, 4), containsSubSet(1, 2, 3));
        assertThat(HashSet.of(1, 2, 3, 4), containsSubSet(List.of(1, 2, 3, 4)));
        assertThat(HashSet.of(1, 2, 3, 4), not(containsSubSet(1, 2, 3, 4, 5)));

        Description description = new StringDescription();
        containsSubSet(1, 2, 3).describeMismatch(HashSet.of(1, 2), description);
        assertThat(description.toString(),
                is("Expected a Set containing all of [<1>,<2>,<3>] but is missing [<3>]"));
    }

    @Test
    public void testIsSubSetOf() {
        assertThat(HashSet.of(1, 2), isSubSetOf(1, 2, 3, 4));
        assertThat(HashSet.of(2, 3), isSubSetOf(1, 2, 3, 4));
        assertThat(HashSet.of(2, 3), isSubSetOf(List.of(1, 2, 3, 4)));
        assertThat(HashSet.of(2, 3), not(isSubSetOf(1, 2)));

        Description description = new StringDescription();
        isSubSetOf(1, 2, 3).describeMismatch(HashSet.of(1, 2, 3, 4), description);
        assertThat(description.toString(),
                is("Expected a Set being a subset of [<1>,<2>,<3>] but contained also [<4>]"));
    }

    @Test
    public void testContainsKey() {
        assertThat(HashMap.of(1, 2, 3, 4), containsKeys(List.of(1, 3)));
        assertThat(HashMap.of(1, 2, 3, 4), containsKeys(1, 3));
        assertThat(HashMap.of(1, 2, 3, 4), not(containsKeys(1, 2)));

        Description description = new StringDescription();
        containsKeys(1, 2).describeMismatch(HashMap.of(1, 2, 3, 4), description);
        assertThat(description.toString(),
                is("Expected a Map containing the following keys [<1>,<2>] but is missing [<2>]"));
    }

    @Test
    public void testContainsValue() {
        assertThat(HashMap.of(1, 2, 3, 4), containsValues(List.of(2, 4)));
        assertThat(HashMap.of(1, 2, 3, 4), containsValues(2, 4));
        assertThat(HashMap.of(1, 2, 3, 4), not(containsValues(2, 3)));

        Description description = new StringDescription();
        containsValues(1, 2).describeMismatch(HashMap.of(1, 2, 3, 4), description);
        assertThat(description.toString(),
                is("Expected a Map containing the following values [<1>,<2>] but is missing [<1>]"));
    }

    @Test
    public void testMapContains() {
        assertThat(HashMap.of(1, 2, 3, 4), contains(1, 2));
        assertThat(HashMap.of(1, 2, 3, 4), contains(3, 4));
        assertThat(HashMap.of(1, 2, 3, 4), not(contains(1, 3)));

        Description description = new StringDescription();
        contains(1, 3).describeMismatch(HashMap.of(1, 2, 3, 4), description);
        assertThat(description.toString(),
                is("Expected a Map containing an entry <1>=<3> but found value <2>"));
    }

    @Test
    public void testIsCancelled() {
        Future<Integer> f = Future.of(() -> {
            Thread.sleep(10_000);
            return 1;
        });
        f.cancel();
        assertThat(f, isCancelled());
        f = Future.of(() -> 1);
        f.get();
        assertThat(f, not(isCancelled()));

        Description description = new StringDescription();
        isCancelled().describeMismatch(Future.of(() -> 1), description);
        assertThat(description.toString(),
                is("Expected a cancelled Future but it was not"));
    }

    @Test
    public void testIsCompleted() {
        Future<Integer> f = Future.of(() -> 1);
        f.get();
        assertThat(f, isCompleted());
        assertThat(f, isCompleted(is(1)));
        f = Future.of(() -> {
            Thread.sleep(10_000);
            return 1;
        });
        assertThat(f, not(isCompleted()));

        Description description = new StringDescription();
        f = Future.of(() -> 1);
        f.get();
        isCompleted().describeMismatch(f, description);
        assertThat(description.toString(),
                is("Expected a completed Future but it was not"));
    }

    @Test
    public void testIsEvaluated() {
        Lazy<Integer> l = Lazy.of(() -> 1);
        l.get();
        assertThat(l, isEvaluated());
        assertThat(l, isEvaluated(is(1)));
        l = Lazy.of(() -> 1);
        assertThat(l, not(isEvaluated()));

        Description description = new StringDescription();
        l = Lazy.of(() -> 1);
        isEvaluated().describeMismatch(l, description);
        assertThat(description.toString(),
                is("Expected an evaluated Lazy but it was not"));
    }

    @Test
    public void testTupleArity() {
        assertThat(Tuple.of(1), hasArity(1));
        assertThat(Tuple.of(1, 2), hasArity(2));
        assertThat(Tuple.of(1, 2), hasArity(is(2)));
        assertThat(Tuple.of(1, 2, 3), hasArity(3));
        assertThat(Tuple.of(1, 2), not(hasArity(5)));

        Description description = new StringDescription();
        hasArity(2).describeMismatch(Tuple.of(1, 2, 3), description);
        assertThat(description.toString(),
                is("Expected a Tuple with arity <2> but found one with arity <3>"));

        description = new StringDescription();
        hasArity(is(2)).describeMismatch(Tuple.of(1, 2, 3), description);
        assertThat(description.toString(),
                is("Expected a Tuple to match arity is <2> but has arity <3>"));
    }

    @Test
    public void testIsValid() {
        assertThat(Validation.valid(1), isValid());
        assertThat(Validation.valid(1), isValid(is(1)));
        assertThat(Validation.invalid(1), not(isValid()));

        Description description = new StringDescription();
        isValid().describeMismatch(Validation.invalid(1), description);
        assertThat(description.toString(),
                is("Expected a valid Validation but it was not"));
    }

    @Test
    public void testIsInvalid() {
        assertThat(Validation.valid(1), not(isInvalid()));
        assertThat(Validation.invalid(1), isInvalid());
        assertThat(Validation.invalid(1), isInvalid(is(1)));

        Description description = new StringDescription();
        isInvalid().describeMismatch(Validation.valid(1), description);
        assertThat(description.toString(),
                is("Expected an invalid Validation but it was not"));
    }

    @Test
    public void readmeExample() {
        Try<Integer> age = Try.of(() -> 30);
        // ensure the Try is a success and its value is less than 40
        assertThat(age, isSuccess(lessThan(40)));

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