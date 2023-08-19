package clausewitz_coding.localization;
 /**
 * This is the Localization record file.
 */
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
