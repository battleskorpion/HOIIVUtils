package clausewitz_coding.localization;

public record LocalizationDescription(String ID, String text, Status status, Localization parent) {

    public enum Status {
        EXISTS,
        NEW,
        UPDATED,
    };

    public String toString() {
        return "parent: " + parent.ID() + ", " + ID + ":0" + " " + "\"" + text + "\"";
    }
}