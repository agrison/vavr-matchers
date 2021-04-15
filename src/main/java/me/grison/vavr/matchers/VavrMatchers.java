package me.grison.vavr.matchers;

import io.vavr.Function1;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Value;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;
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
import static org.hamcrest.Matchers.is;

@UtilityClass
public class VavrMatchers {
    //region Values & Controls

    //region Option
    public static <T> Matcher<Value<T>> isDefined(Matcher<T> matcher) {
        return typeSafeMatcher(
                v -> v.map(matcher::matches).getOrElse(false),
                description -> description.appendValue("Expected a value with content matching ")
                        .appendDescriptionOf(matcher),
                (v, mismatch) -> v.toOption().onEmpty(() -> mismatch.appendText("No value defined"))
                        .peek(value -> matcher.describeMismatch(value, mismatch)));
    }

    public static <T> Matcher<Value<T>> isDefined() {
        return isDefined(new IsAnything<>());
    }

    public static <T> Matcher<Value<T>> isEmpty() {
        return typeSafeMatcher(
                Value::isEmpty,
                description -> description.appendText("Expected an empty value"),
                (v, mismatch) -> {
                    List<T> values = v.collect(Collectors.toList());
                    v.peek(value -> mismatch.appendText("Expected an empty value but found "))
                            .peek(val -> mismatch.appendValue(values.size() > 1 ? values : val));
                });
    }
    //endregion

    //region Try
    public static <T> Matcher<Try<T>> isSuccess(Matcher<T> matcher) {
        return typeSafeMatcher(
                v -> v.map(matcher::matches).getOrElse(false),
                description -> description.appendValue("Expected a <Success> with content matching: ")
                        .appendDescriptionOf(matcher),
                (v, mismatch) -> v.toTry()
                        .onFailure(e -> mismatch.appendText("Expected a <Success> but found ").appendValue(failure(e)))
                        .onSuccess(val -> matcher.describeMismatch(val,
                                mismatch.appendText("Expected a <Success> with content matching `")
                                        .appendDescriptionOf(matcher)
                                        .appendText("` but "))));
    }

    public static <T> Matcher<Try<T>> isSuccess() {
        return isSuccess(new IsAnything<>());
    }

    public static <T> Matcher<Try<T>> isFailure() {
        return typeSafeMatcher(
                Try::isFailure,
                description -> description.appendText("Expected a <Failure> but it was not"),
                (v, mismatch) -> v.onSuccess(val -> mismatch.appendText("Expected a <Failure> but found ")
                        .appendValue(success(val))));
    }

    public static <T, E extends Throwable> Matcher<Try<T>> isFailure(Class<E> clazz) {
        return typeSafeMatcher(
                t -> t.isFailure() && clazz.equals(t.getCause().getClass()),
                description -> description.appendText("Expected a <Failure(").appendText(clazz.getName()).appendText(")>"),
                (t, mismatch) -> t.onFailure(cause -> mismatch
                        .appendText("Expected ")
                        .appendText("<Failure(" + clazz.getName() + ")>")
                        .appendText(" but found ")
                        .appendText("<Failure(" + cause.getClass().getName() + ")>"))
                        .onSuccess(val -> mismatch.appendText("Expected <Failure()> but found ").appendValue(success(val))));
    }
    //endregion

    //region Either
    public static <L, R> Matcher<Either<L, R>> isRight(Matcher<R> matcher) {
        return typeSafeMatcher(
                e -> e.map(matcher::matches).getOrElse(false),
                description -> description.appendText("Expected a <Right> with content matching ").appendDescriptionOf(matcher),
                (e, mismatch) -> e
                        .peek(r -> matcher.describeMismatch(r,
                                mismatch.appendText("Expected a <Right> with content matching `")
                                        .appendDescriptionOf(matcher).appendText("` but ")))
                        .peekLeft(l -> mismatch.appendText("Expected a <Right> but got ").appendValue(left(l))));
    }

    public static <L, R> Matcher<Either<L, R>> isRight() {
        return isRight(new IsAnything<>());
    }

    public static <L, R> Matcher<Either<L, R>> isLeft(Matcher<L> matcher) {
        return typeSafeMatcher(
                e -> e.isLeft() && e.mapLeft(matcher::matches).getLeft(),
                description -> description.appendText("Expected a <Left> with content matching ").appendDescriptionOf(matcher),
                (e, mismatch) -> e
                        .peekLeft(r -> matcher.describeMismatch(r,
                                mismatch.appendText("Expected a <Left> with content matching `")
                                        .appendDescriptionOf(matcher).appendText("` but ")))
                        .peek(l -> mismatch.appendText("Expected a <Left> but got ").appendValue(right(l))));
    }

    public static <L, R> Matcher<Either<L, R>> isLeft() {
        return isLeft(new IsAnything<>());
    }
    //endregion

    //endregion

    //region Traversable
    public static <T> Matcher<Traversable<T>> hasLength(int length) {
        return typeSafeMatcher(
                t -> t.length() == length,
                description -> description.appendText("Expected Traversable to have length ").appendValue(length),
                (t, mismatch) -> mismatch.appendText("Expected Traversable to have length ").appendValue(length)
                        .appendText(" but has length ")
                        .appendValue(t.length())
        );
    }

    public static <T> Matcher<Traversable<T>> hasLength(Matcher<Integer> length) {
        return typeSafeMatcher(
                t -> length.matches(t.length()),
                description -> description.appendText("Expected Traversable to match length ").appendDescriptionOf(length),
                (t, mismatch) -> mismatch.appendText("Expected Traversable to match length ").appendDescriptionOf(length)
                        .appendText(" but has length ")
                        .appendValue(t.length())
        );
    }

    public static <T> Matcher<Traversable<T>> contains(T element) {
        return contains(is(element));
    }

    public static <T> Matcher<Traversable<T>> contains(Matcher<T> matcher) {
        return typeSafeMatcher(
                t -> t.find(matcher::matches).isDefined(),
                description -> description.appendText("Expected at least one element matching ").appendDescriptionOf(matcher),
                (t, mismatch) -> mismatch.appendText("Expected at least one element matching `").appendDescriptionOf(matcher)
                        .appendText("' but found ")
                        .appendValue(t)
        );
    }

    @SafeVarargs
    public static <T> Matcher<Traversable<T>> containsSubList(T... items) {
        return containsSubList(Vector.of(items));
    }

    public static <T> Matcher<Traversable<T>> containsSubList(Traversable<T> items) {
        return typeSafeMatcher(
                t -> t.mkString().contains(items.mkString()),
                description -> description.appendText("Expected a Traversable containing in same order all of ")
                        .appendValueList("[", ",", "]", items),
                (t, mismatch) -> mismatch.appendText("Expected a Traversable containing in same order all of ")
                        .appendValueList("[", ",", "]", items)
                        .appendText(" but is missing ")
                        .appendValueList("[", ",", "]", items.partition(t::contains)._2)
        );
    }

    @SafeVarargs
    public static <T> Matcher<Traversable<T>> containsInAnyOrder(T... items) {
        return containsInAnyOrder(Vector.of(items));
    }

    public static <T> Matcher<Traversable<T>> containsInAnyOrder(Traversable<T> items) {
        return typeSafeMatcher(
                t -> t.containsAll(items),
                description -> description.appendText("Expected a Traversable containing all of ")
                        .appendValueList("[", ",", "]", items),
                (t, mismatch) -> mismatch.appendText("Expected a Traversable containing all of ")
                        .appendValueList("[", ",", "]", items)
                        .appendText(" but is missing ")
                        .appendValueList("[", ",", "]", items.partition(t::contains)._2)
        );
    }

    public static <T> Matcher<Traversable<T>> allMatch(Matcher<T> matcher) {
        return typeSafeMatcher(
                t -> t.forAll(matcher::matches),
                description -> description.appendText("Expected a Traversable where all elements should match ")
                        .appendDescriptionOf(matcher),
                (t, mismatch) -> mismatch.appendText("Expected a Traversable where all elements should match ")
                        .appendDescriptionOf(matcher)
                        .appendText(" but found non-matching elements ")
                        .appendValueList("[", ",", "]", t.filter(e -> !matcher.matches(e)).toJavaList())
        );
    }

    public static <T> Matcher<Seq<T>> isSorted() {
        return typeSafeMatcher(
                t -> t.sorted().equals(t),
                description -> description.appendText("Expected a Seq to be sorted but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a Seq to be sorted but it was not")
        );
    }

    public static <T> Matcher<Seq<T>> isReverseSorted() {
        return typeSafeMatcher(
                t -> t.sorted().reverse().equals(t),
                description -> description.appendText("Expected a Seq to be reverse sorted but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a Seq to be reverse sorted but it was not")
        );
    }

    @SafeVarargs
    public static <T> Matcher<Seq<T>> startsWith(T... items) {
        return startsWith(Vector.of(items));
    }

    public static <T> Matcher<Seq<T>> startsWith(Traversable<T> items) {
        return typeSafeMatcher(
                t -> t.startsWith(items),
                description -> description.appendText("Expected a Seq to start with ")
                        .appendValueList("[", ",", "]", items),
                (t, mismatch) -> mismatch.appendText("Expected a Seq to start with ")
                        .appendValueList("[", ",", "]", items)
                        .appendText(" but found a Seq starting with ")
                        .appendValueList("[", ",", "]", t.subSequence(0, items.size()))
        );
    }

    @SafeVarargs
    public static <T> Matcher<Seq<T>> endsWith(T... items) {
        return endsWith(Vector.of(items));
    }

    public static <T> Matcher<Seq<T>> endsWith(Seq<T> items) {
        return typeSafeMatcher(
                t -> t.endsWith(items),
                description -> description.appendText("Expected a Seq to end with ")
                        .appendValueList("[", ",", "]", items),
                (t, mismatch) -> mismatch.appendText("Expected a Seq to end with ")
                        .appendValueList("[", ",", "]", items)
                        .appendText(" but found a Seq ending with ")
                        .appendValueList("[", ",", "]", items.size() > t.size() ? t : t.subSequence(t.size() - items.size()))
        );
    }

    public static <T> Matcher<Seq<T>> isUnique() {
        return typeSafeMatcher(
                t -> t.toSet().size() == t.size(),
                description -> description.appendText("Expected a Seq to have unique elements"),
                (t, mismatch) -> mismatch
                        .appendText("Expected a Seq to have unique elements but found the following duplicate elements ")
                        .appendValueList("[", ",", "]",
                                t.toSet().filter(e -> t.count(x -> x.equals(e)) > 1))
        );
    }
    //endregion

    //region Map
    @SafeVarargs
    public static <T, U> Matcher<Map<T, U>> containsKeys(T... items) {
        return containsKeys(Vector.of(items));
    }

    public static <T, U> Matcher<Map<T, U>> containsKeys(Traversable<T> items) {
        return typeSafeMatcher(
                t -> t.keySet().containsAll(items),
                description -> description.appendText("Expected a Map containing the following keys ")
                        .appendValueList("[", ",", "]", items),
                (t, mismatch) -> mismatch.appendText("Expected a Map containing the following keys ")
                        .appendValueList("[", ",", "]", items)
                        .appendText(" but is missing ")
                        .appendValueList("[", ",", "]", items.partition(e -> t.keySet().contains(e))._2)
        );
    }

    @SafeVarargs
    public static <T, U> Matcher<Map<T, U>> containsValues(U... items) {
        return containsValues(Vector.of(items));
    }

    public static <T, U> Matcher<Map<T, U>> containsValues(Traversable<U> items) {
        return typeSafeMatcher(
                t -> t.values().containsAll(items),
                description -> description.appendText("Expected a Map containing the following values ")
                        .appendValueList("[", ",", "]", items),
                (t, mismatch) -> mismatch.appendText("Expected a Map containing the following values ")
                        .appendValueList("[", ",", "]", items)
                        .appendText(" but is missing ")
                        .appendValueList("[", ",", "]", items.partition(e -> t.values().contains(e))._2)
        );
    }

    public static <T, U> Matcher<Map<T, U>> contains(T key, U value) {
        return typeSafeMatcher(
                t -> t.get(key).map(value::equals).getOrElse(false),
                description -> description.appendText("Expected a Map containing an entry ")
                        .appendValue(key).appendText("=").appendValue(value),
                (t, mismatch) -> mismatch.appendText("Expected a Map containing an entry ")
                        .appendValue(key).appendText("=").appendValue(value)
                        .appendText(" but found ").appendText(t.get(key).isDefined() ? "value <" + t.get(key).get() + ">" : "no such key")
        );
    }
    //endregion

    //region Future
    public static <T> Matcher<Future<T>> isCancelled() {
        return typeSafeMatcher(
                Future::isCancelled,
                description -> description.appendText("Expected a cancelled Future but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a cancelled Future but it was not")
        );
    }

    public static <T> Matcher<Future<T>> isCompleted() {
        return typeSafeMatcher(
                Future::isCompleted,
                description -> description.appendText("Expected a completed Future but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a completed Future but it was not")
        );
    }

    public static <T> Matcher<Future<T>> isCompleted(Matcher<T> matcher) {
        return typeSafeMatcher(
                t -> t.isCompleted() && t.map(matcher::matches).getOrElse(false),
                description -> description.appendText("Expected a completed Future but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a completed Future but it was not")
        );
    }
    //endregion

    //region Lazy
    public static <T> Matcher<Lazy<T>> isEvaluated() {
        return typeSafeMatcher(
                Lazy::isEvaluated,
                description -> description.appendText("Expected an evaluated Lazy but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected an evaluated Lazy but it was not")
        );
    }

    public static <T> Matcher<Lazy<T>> isEvaluated(Matcher<T> matcher) {
        return typeSafeMatcher(
                t -> t.isEvaluated() && t.map(matcher::matches).getOrElse(false),
                description -> description.appendText("Expected an evaluated Lazy but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected an evaluated Lazy but it was not")
        );
    }
    //endregion

    //region Tuple
    public static <T> Matcher<Tuple> hasArity(int arity) {
        return typeSafeMatcher(
                t -> t.arity() == arity,
                description -> description.appendText("Expected a Tuple with arity ").appendValue(arity),
                (t, mismatch) -> mismatch.appendText("Expected a Tuple with arity ").appendValue(arity)
                        .appendText(" but found one with arity ").appendValue(t.arity())
        );
    }

    public static <T> Matcher<Tuple> hasArity(Matcher<Integer> length) {
        return typeSafeMatcher(
                t -> length.matches(t.arity()),
                description -> description.appendText("Expected a Tuple to match arity ").appendDescriptionOf(length),
                (t, mismatch) -> mismatch.appendText("Expected a Tuple to match arity ").appendDescriptionOf(length)
                        .appendText(" but has arity ")
                        .appendValue(t.arity())
        );
    }
    //endregion

    //region Validation
    public static <T, U> Matcher<Validation<T, U>> isValid() {
        return typeSafeMatcher(
                Validation::isValid,
                description -> description.appendText("Expected a valid Validation but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a valid Validation but it was not")
        );
    }

    public static <T, U> Matcher<Validation<T, U>> isValid(Matcher<U> matcher) {
        return typeSafeMatcher(
                t -> t.isValid() && t.map(matcher::matches).getOrElse(false),
                description -> description.appendText("Expected a valid Validation but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a valid Validation but it was not")
        );
    }

    public static <T, U> Matcher<Validation<T, U>> isInvalid() {
        return typeSafeMatcher(
                Validation::isInvalid,
                description -> description.appendText("Expected an invalid Validation but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected an invalid Validation but it was not")
        );
    }

    public static <T, U> Matcher<Validation<T, U>> isInvalid(Matcher<T> matcher) {
        return typeSafeMatcher(
                t -> t.isInvalid() && t.mapError(matcher::matches).getError(),
                description -> description.appendText("Expected a valid Validation but it was not"),
                (t, mismatch) -> mismatch.appendText("Expected a valid Validation but it was not")
        );
    }
    //endregion

    //region TypeSafeMatcher
    private static <T> TypeSafeMatcher<T> typeSafeMatcher(Function1<T, Boolean> matches,
                                                          Consumer<Description> describes,
                                                          BiConsumer<T, Description> describesMismatch) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T t) {
                return matches.apply(t);
            }

            @Override
            public void describeTo(Description description) {
                describes.accept(description);
            }

            @Override
            public void describeMismatchSafely(T t, Description mismatch) {
                describesMismatch.accept(t, mismatch);
            }
        };
    }
    //endregion
}
