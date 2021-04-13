package me.grison.vavr.matchers;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static me.grison.vavr.matchers.VavrMatchers.*;
import static me.grison.vavr.matchers.VavrMatchers.hasLength;
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
        assertThat(description.toString(), is("Expected empty but found <1337>"));
    }

    @Test
    public void testIsSuccess() {
        assertThat(Try.success("foo"), isSuccess());
        assertThat(Try.of(() -> "foo"), isSuccess());
        assertThat(Try.success("foo"), isSuccess(not(emptyString())));

        Description description = new StringDescription();
        isSuccess().describeMismatch(Try.failure(new Exception()), description);
        assertThat(description.toString(),
                is("Expected <Success()> but found <Failure(java.lang.Exception)>"));

        assertThat(Try.failure(new Exception()), not(isSuccess()));
        assertThat(Try.of(() -> {
            throw new Exception();
        }), not(isSuccess()));
        assertThat(Try.failure(new Exception()), not(isSuccess(anything())));

        description = new StringDescription();
        isSuccess(lessThan(36)).describeMismatch(Try.success(1337), description);
        assertThat(description.toString(),
                is("Expected <Success()> content matching `a value less than <36>` but <1337> was greater than <36>"));
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
                is("Expected <Failure()> but found <Success(foo)>"));
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
        assertThat(description.toString(), is("Expected <Right()> but got <Left(foo)>"));

        description = new StringDescription();
        isRight(is(1)).describeMismatch(Either.right(36), description);
        assertThat(description.toString(), is("Expected <Right()> whose content was <36>"));
    }

    @Test
    public void testIsLeft() {
        assertThat(Either.left(1), isLeft());
        assertThat(Either.right(1), not(isLeft()));

        Description description = new StringDescription();
        isLeft().describeMismatch(Either.right("foo"), description);
        assertThat(description.toString(), is("Expected <Left()> but got <Right(foo)>"));

        description = new StringDescription();
        isLeft(is(1)).describeMismatch(Either.left(36), description);
        assertThat(description.toString(), is("Expected <Left()> whose content was <36>"));
    }
    
    @Test
    public void testCollectionIsEmpty() {
        assertThat(List.empty(), isEmpty());
        assertThat(List.of(1), not(isEmpty()));

        Description description = new StringDescription();
        isEmpty().describeMismatch(List.of(1, 2, 3), description);
        assertThat(description.toString(), is("Expected empty but found <[1, 2, 3]>"));
    }

    @Test
    public void testHasLength() {
        assertThat(List.of(1, 2, 3, 4, 5), hasLength(5));
        assertThat(List.empty(), not(hasLength(5)));

        assertThat(List.of(1, 2, 3), hasLength(lessThan(20)));
        assertThat(List.of(1, 2, 3), not(hasLength(lessThan(2))));

        Description description = new StringDescription();
        hasLength(1).describeMismatch(List.of(1, 2), description);
        assertThat(description.toString(), is("Expected Traversable to have length <1> but has length <2>"));

        description = new StringDescription();
        hasLength(lessThan(2)).describeMismatch(List.of(1, 2, 3), description);
        assertThat(description.toString(), is("Expected Traversable to match length a value less than <2> but has length <3>"));
    }
}