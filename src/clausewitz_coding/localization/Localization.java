package clausewitz_coding.localization;

public record Localization(String ID, String text, Status status) {

    public enum Status {
        EXISTS,
        NEW,
        UPDATED,
    };

    public String toString() {
        return ID + ":0" + " " + "\"" + text + "\"";
    }
}
