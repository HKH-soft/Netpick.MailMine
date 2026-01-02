package ir.netpick.mailmine.common.result;

import ir.netpick.mailmine.common.result.error.Error;
import ir.netpick.mailmine.common.result.success.Created;
import ir.netpick.mailmine.common.result.success.Deleted;
import ir.netpick.mailmine.common.result.success.Success;
import ir.netpick.mailmine.common.result.success.Updated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

// --- Generic Result wrapper (sealed interface for type safety)
public sealed interface Result<T> {

    // --- Factory methods
    // Success with value (disallow null to avoid confusion)
    static <T> Result<T> ok(T value) {
        return new Ok<>(Objects.requireNonNull(value, "Success value cannot be null"));
    }

    // Success with no value
    static Result<Success> ok() {
        return new Ok<>(Success.INSTANCE);
    }

    // Created
    static Result<Created> created() {
        return new Ok<>(Created.INSTANCE);
    }

    // Updated
    static Result<Updated> updated() {
        return new Ok<>(Updated.INSTANCE);
    }

    // Deleted
    static Result<Deleted> deleted() {
        return new Ok<>(Deleted.INSTANCE);
    }

    // --- Common error factory methods
    static <T> Result<T> validationError(String message) {
        return error(new Error("General.VALIDATION_ERROR", message));
    }

    static <T> Result<T> notFound(String resourceName) {
        return error(new Error("General.NOT_FOUND", String.format("%s not found", resourceName)));
    }

    static <T> Result<T> conflict(String message) {
        return error(new Error("General.CONFLICT", message));
    }

    static <T> Result<T> unexpected(String message) {
        return error(new Error("General.UNEXPECTED", message));
    }

    // Single error
    static <T> Result<T> error(Error error) {
        return new Err<>(List.of(error));
    }

    // Multiple errors
    static <T> Result<T> errors(List<Error> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("Errors list cannot be null or empty");
        }
        return new Err<>(List.copyOf(errors));
    }

    // --- Query methods
    boolean isSuccess();

    boolean isError();

    T getValue(); // Throws if error

    List<Error> getErrors(); // Empty if success

    // --- Functional methods
    // Map the value if success, else propagate errors
    default <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        return this instanceof Ok<T> ok ? ok(mapper.apply(ok.value())) : (Result<U>) this;
    }

    // FlatMap for chaining results
    default <U> Result<U> flatMap(Function<? super T, ? extends Result<? extends U>> mapper) {
        return this instanceof Ok<T> ok ? (Result<U>) mapper.apply(ok.value()) : (Result<U>) this;
    }

    // Provide alternative if error
    default T orElse(Function<? super List<Error>, ? extends T> errorHandler) {
        return this instanceof Err<T> err ? errorHandler.apply(err.errors()) : getValue();
    }

    // Fold method for comprehensive pattern matching
    default <R> R fold(
            Function<? super List<Error>, ? extends R> onError,
            Function<? super T, ? extends R> onSuccess) {
        return this instanceof Err<T> err ? onError.apply(err.errors()) : onSuccess.apply(((Ok<T>) this).value());
    }

    // --- Implementations
    record Ok<T>(T value) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public List<Error> getErrors() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return "Ok(" + value + ")";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Ok<?> ok && Objects.equals(value, ok.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    record Err<T>(List<Error> errors) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public T getValue() {
            throw new IllegalStateException("Cannot get value from Err: " + errors);
        }

        @Override
        public List<Error> getErrors() {
            return errors;
        }

        @Override
        public String toString() {
            return "Err(" + errors + ")";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Err<?> err && Objects.equals(errors, err.errors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errors);
        }
    }
}