package ir.netpick.mailmine.common.result.error;

public class GeneralErrors {
    public static final Error VALIDATION_ERROR = new Error("General.VALIDATION_ERROR", "Validation error");
    public static final Error NOT_FOUND = new Error("General.NOT_FOUND", "Resource not found");
    public static final Error CONFLICT = new Error("General.CONFLICT", "Resource already exists");
    public static final Error UNEXPECTED = new Error("General.UNEXPECTED", "Unexpected error");
}
