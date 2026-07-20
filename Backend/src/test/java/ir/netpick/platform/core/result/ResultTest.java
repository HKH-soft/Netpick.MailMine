package ir.netpick.platform.core.result;

import ir.netpick.platform.core.result.error.Error;
import ir.netpick.platform.core.result.success.Created;
import ir.netpick.platform.core.result.success.Deleted;
import ir.netpick.platform.core.result.success.Success;
import ir.netpick.platform.core.result.success.Updated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Nested
    @DisplayName("Success Factory Methods")
    class SuccessFactoryTests {
        @Test
        @DisplayName("ok(T value) should create successful result")
        void okWithValue() {
            Result<String> result = Result.ok("test");

            assertTrue(result.isSuccess());
            assertEquals("test", result.getValue());
            assertTrue(result.getErrors().isEmpty());
        }

        @Test
        @DisplayName("ok() should create successful result with Success.INSTANCE")
        void okNoValue() {
            Result<Success> result = Result.ok();

            assertTrue(result.isSuccess());
            assertSame(Success.INSTANCE, result.getValue());
        }

        @Test
        @DisplayName("created() should return Created.INSTANCE")
        void created() {
            Result<Created> result = Result.created();

            assertTrue(result.isSuccess());
            assertSame(Created.INSTANCE, result.getValue());
        }

        @Test
        @DisplayName("updated() should return Updated.INSTANCE")
        void updated() {
            Result<Updated> result = Result.updated();

            assertTrue(result.isSuccess());
            assertSame(Updated.INSTANCE, result.getValue());
        }

        @Test
        @DisplayName("deleted() should return Deleted.INSTANCE")
        void deleted() {
            Result<Deleted> result = Result.deleted();

            assertTrue(result.isSuccess());
            assertSame(Deleted.INSTANCE, result.getValue());
        }

        @Test
        @DisplayName("ok(T value) should reject null value")
        void okRejectsNull() {
            assertThrows(NullPointerException.class, () -> Result.ok(null));
        }
    }

    @Nested
    @DisplayName("Error Factory Methods")
    class ErrorFactoryTests {
        @Test
        @DisplayName("error(Error) should create error result")
        void errorSingle() {
            Error error = new Error("TEST.CODE", "Test message");
            Result<String> result = Result.error(error);

            assertTrue(result.isError());
            assertEquals(1, result.getErrors().size());
            assertEquals(error, result.getErrors().get(0));
            assertThrows(IllegalStateException.class, result::getValue);
        }

        @Test
        @DisplayName("errors(List) should create multiple errors")
        void errorsMultiple() {
            List<Error> errors = List.of(
                    new Error("CODE1", "Message 1"),
                    new Error("CODE2", "Message 2")
            );
            Result<String> result = Result.errors(errors);

            assertTrue(result.isError());
            assertEquals(2, result.getErrors().size());
        }

        @Test
        @DisplayName("errors(null) should throw")
        void errorsRejectsNull() {
            assertThrows(IllegalArgumentException.class, () -> Result.errors(null));
        }

        @Test
        @DisplayName("errors(empty) should throw")
        void errorsRejectsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> Result.errors(List.of()));
        }
    }

    @Nested
    @DisplayName("Predefined Error Methods")
    class PredefinedErrorTests {
        @Test
        @DisplayName("validationError should create validation error")
        void validationError() {
            Result<String> result = Result.validationError("Invalid input");

            assertTrue(result.isError());
            assertEquals("General.VALIDATION_ERROR", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("notFound should create not found error")
        void notFound() {
            Result<String> result = Result.notFound("User");

            assertTrue(result.isError());
            assertEquals("General.NOT_FOUND", result.getErrors().get(0).code());
            assertTrue(result.getErrors().get(0).message().contains("User"));
        }

        @Test
        @DisplayName("conflict should create conflict error")
        void conflict() {
            Result<String> result = Result.conflict("Resource exists");

            assertTrue(result.isError());
            assertEquals("General.CONFLICT", result.getErrors().get(0).code());
        }

        @Test
        @DisplayName("unexpected should create unexpected error")
        void unexpected() {
            Result<String> result = Result.unexpected("Something went wrong");

            assertTrue(result.isError());
            assertEquals("General.UNEXPECTED", result.getErrors().get(0).code());
        }
    }

    @Nested
    @DisplayName("Functional Methods")
    class FunctionalMethodsTests {
        @Test
        @DisplayName("map should transform success value")
        void mapSuccess() {
            Result<String> result = Result.ok("test")
                    .map(String::toUpperCase);

            assertEquals("TEST", result.getValue());
        }

        @Test
        @DisplayName("map should propagate errors")
        void mapError() {
            Result<String> result = Result.error(new Error("CODE", "Message"));

            assertTrue(result.isError());
        }

        @Test
        @DisplayName("flatMap should chain results")
        void flatMapSuccess() {
            Result<String> result = Result.ok("test")
                    .flatMap(v -> Result.ok(v.toUpperCase()));

            assertEquals("TEST", result.getValue());
        }

        @Test
        @DisplayName("orElse should provide alternative for error")
        void orElseError() {
            Result<String> result = Result.error(new Error("CODE", "Message"));

            String fallback = result.orElse(e -> "fallback");

            assertEquals("fallback", fallback);
        }

        @Test
        @DisplayName("fold should handle both cases")
        void fold() {
            String successResult = Result.ok("value")
                    .fold(
                            errors -> "error",
                            value -> "success: " + value
                    );

            assertEquals("success: value", successResult);

            String errorResult = Result.error(new Error("CODE", "Message"))
                    .fold(
                            errors -> "error: " + errors.get(0).code(),
                            value -> "success"
                    );

            assertEquals("error: CODE", errorResult);
        }
    }

    @Nested
    @DisplayName("Ok Record Tests")
    class OkTests {
        @Test
        @DisplayName("equals should compare values")
        void equals() {
            Result<String> r1 = Result.ok("test");
            Result<String> r2 = Result.ok("test");
            Result<String> r3 = Result.ok("other");

            assertEquals(r1, r2);
            assertNotEquals(r1, r3);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeConsistency() {
            Result<String> r1 = Result.ok("test");
            Result<String> r2 = Result.ok("test");

            assertEquals(r1.hashCode(), r2.hashCode());
        }
    }

    @Nested
    @DisplayName("Err Record Tests")
    class ErrTests {
        @Test
        @DisplayName("equals should compare errors")
        void equals() {
            Result<String> r1 = Result.error(new Error("CODE", "Message"));
            Result<String> r2 = Result.error(new Error("CODE", "Message"));
            Result<String> r3 = Result.error(new Error("OTHER", "Other"));

            assertEquals(r1, r2);
            assertNotEquals(r1, r3);
        }
    }
}