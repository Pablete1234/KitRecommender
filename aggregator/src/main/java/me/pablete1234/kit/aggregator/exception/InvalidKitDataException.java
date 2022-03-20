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
        FEW_RECORDS("File has too few records"),
        SMALL_TIMEFRAME("File represents too small of a timeframe"),
        CORRUPTED_DATA("File includes corrupted records"),
        NO_KIT_GIVEN("No kit was given to the player"),
        DIFFERING_KITS("The kits given were different thru the match");

        final String message;

        Reason(String message) {
            this.message = message;
        }
    }

}
