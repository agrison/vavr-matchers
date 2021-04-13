package me.grison.vavr.matchers;

import io.vavr.Function1;
import io.vavr.Value;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import io.vavr.collection.Vector;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsAnything;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static io.vavr.control.Try.failure;
import static io.vavr.control.Try.success;

@UtilityClass
public class VavrMatchers {
    //region Values & Controls
    private static <T, U extends Value<T>> TypeSafeMatcher<U> valueTypeSafeMatcher(
            Function1<Value<T>, Boolean> matches,
            Consumer<Description> describes,
            BiConsumer<Value<T>, Description> describesMismatch) {
        return new TypeSafeMatcher<U>() {
            @Override
            protected boolean matchesSafely(U t) {
                return matches.apply(t);
            }

            @Override
            public void describeTo(Description description) {
                describes.accept(description);
            }

            @Override
            public void describeMismatchSafely(U t, Description mismatch) {
                describesMismatch.accept(t, mismatch);
            }
        };
    }

    //region Option
    public static <T> Matcher<Value<T>> isDefined(Matcher<T> matcher) {
        return valueTypeSafeMatcher(
                v -> v.map(matcher::matches).getOrElse(false),
                description -> description.appendValue("Value that contains value matching ")
                        .appendDescriptionOf(matcher),
                (v, mismatch) -> v.toOption().onEmpty(() -> mismatch.appendText("No value defined"))
                        .peek(value -> matcher.describeMismatch(value, mismatch)));
    }

    public static <T> Matcher<Value<T>> isDefined() {
        return isDefined(new IsAnything<>());
    }

    public static <T> Matcher<Value<T>> isEmpty() {
        return valueTypeSafeMatcher(
                Value::isEmpty,
                description -> description.appendText("The Value was not empty"),
                (v, mismatch) -> {
                    List<T> values = v.collect(Collectors.toList());
                    v.peek(value -> mismatch.appendText("Expected empty but found "))
                            .peek(val -> mismatch.appendValue(values.size() > 1 ? values : val));
                });
    }
    //endregion

    //region Try
    public static <T> Matcher<Try<T>> isSuccess(Matcher<T> matcher) {
        return valueTypeSafeMatcher(
                v -> v.map(matcher::matches).getOrElse(false),
                description -> description.appendValue("Expected <Success()> content matching: ")
                        .appendDescriptionOf(matcher),
                (v, mismatch) -> v.toTry()
                        .onFailure(e -> mismatch.appendText("Expected <Success()> but found ").appendValue(failure(e)))
                        .onSuccess(val -> matcher.describeMismatch(val,
                                mismatch.appendText("Expected <Success()> content matching `")
                                        .appendDescriptionOf(matcher)
                                        .appendText("` but "))));
    }

    public static <T> Matcher<Try<T>> isSuccess() {
        return isSuccess(new IsAnything<>());
    }

    public static <T> Matcher<Try<T>> isFailure() {
        return valueTypeSafeMatcher(
                v -> v.toTry().isFailure(),
                description -> description.appendText("Failure()"),
                (v, mismatch) -> v.toTry()
                        .onSuccess(val -> mismatch.appendText("Expected <Failure()> but found ")
                                .appendValue(success(val))));
    }

    public static <T, E extends Throwable> Matcher<Try<T>> isFailure(Class<E> clazz) {
        return valueTypeSafeMatcher(
                t -> t.toTry().isFailure() && clazz.equals(t.toTry().getCause().getClass()),
                description -> description.appendText("Expected <Failure(").appendText(clazz.getName()).appendText(")>"),
                (t, mismatch) -> t.toTry()
                        .onFailure(cause -> mismatch
                                .appendText("Expected ")
                                .appendText("<Failure(" + clazz.getName() + ")>")
                                .appendText(" but found ")
                                .appendText("<Failure(" + cause.getClass().getName() + ")>"))
                        .onSuccess(val -> mismatch.appendText("Expected <Failure()> but found ").appendValue(success(val))));
    }
    //endregion

    //region Either
    public static <L, R> Matcher<Either<L, R>> isRight(Matcher<R> matcher) {
        return VavrMatchers.valueTypeSafeMatcher(
                e -> e.toEither("Left").map(matcher::matches).getOrElse(false),
                description -> description.appendText("Expected <Right()> whose content ").appendDescriptionOf(matcher),
                (e, mismatch) -> ((Either<L, R>) e)
                        .peek(r -> matcher.describeMismatch(r, mismatch.appendText("Expected <Right()> whose content ")))
                        .peekLeft(l -> mismatch.appendText("Expected <Right()> but got ").appendValue(left(l))));
    }

    public static <L, R> Matcher<Either<L, R>> isRight() {
        return isRight(new IsAnything<>());
    }

    public static <L, R> Matcher<Either<L, R>> isLeft(Matcher<L> matcher) {
        return VavrMatchers.valueTypeSafeMatcher(
                e -> ((Either<L, R>) e).isLeft() && ((Either<L, R>) e).mapLeft(matcher::matches).getLeft(),
                description -> description.appendText("Expected <Left()> whose content ").appendDescriptionOf(matcher),
                (e, mismatch) -> ((Either<L, R>) e)
                        .peekLeft(r -> matcher.describeMismatch(r, mismatch.appendText("Expected <Left()> whose content ")))
                        .peek(l -> mismatch.appendText("Expected <Left()> but got ").appendValue(right(l))));
    }

    public static <L, R> Matcher<Either<L, R>> isLeft() {
        return isLeft(new IsAnything<>());
    }
    //endregion

    //endregion

    //region Traversable
    private static <T> TypeSafeMatcher<Traversable<T>> traversableTypeSafeMatcher(
            Function1<Traversable<T>, Boolean> matches,
            Consumer<Description> describes,
            BiConsumer<Traversable<T>, Description> describesMismatch) {
        return new TypeSafeMatcher<Traversable<T>>() {
            @Override
            protected boolean matchesSafely(Traversable<T> t) {
                return matches.apply(t);
            }

            @Override
            public void describeTo(Description description) {
                describes.accept(description);
            }

            @Override
            public void describeMismatchSafely(Traversable<T> t, Description mismatch) {
                describesMismatch.accept(t, mismatch);
            }
        };
    }

    public static <T> Matcher<Traversable<T>> hasLength(int length) {
        return traversableTypeSafeMatcher(
                t -> t.length() == length,
                description -> description.appendText("Expected Traversable to have length ").appendValue(length),
                (t, mismatch) -> mismatch.appendText("Expected Traversable to have length ").appendValue(length)
                        .appendText(" but has length ")
                        .appendValue(t.length())
        );
    }

    public static <T> Matcher<Traversable<T>> hasLength(Matcher<Integer> length) {
        return traversableTypeSafeMatcher(
                t -> length.matches(t.length()),
                description -> description.appendText("Expected Traversable to match length ").appendDescriptionOf(length),
                (t, mismatch) -> mismatch.appendText("Expected Traversable to match length ").appendDescriptionOf(length)
                        .appendText(" but has length ")
                        .appendValue(t.length())
        );
    }

    //endregion
}
