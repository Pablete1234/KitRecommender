package me.pablete1234.kit.aggregator.exception;

public class InvalidKitDataException extends Exception {

    private final Reason reason;

    public InvalidKitDataException(Reason reason, Throwable cause) {
        super(reason.message, cause);
        this.reason = reason;
    }

    public InvalidKitDataException(Reason reason) {
        super(reason.message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        FAILED_READ("Failed to read file"),
        TOO_FEW_RECORDS("File has too few records"),
        CORRUPTED_DATA("File includes corrupted records"),
        NO_KIT_GIVEN("No kit was given to the player"),
        SMALL_KIT_GIVEN("Given kit is too small to sort (single item)"),
        DIFFERING_KITS("The kits given were different throughout the match");

        final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
